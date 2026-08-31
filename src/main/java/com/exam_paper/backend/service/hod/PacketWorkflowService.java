package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacketWorkflowService {

    private final ExamPacketRepository examPacketRepository;
    private final PacketMovementRepository packetMovementRepository;
    private final PacketAssignmentRepository packetAssignmentRepository;

    public List<DepartmentPacketResponseDto> getAllDepartmentPackets(String deptId) {
        return examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getDepartment() != null
                        && deptId.equalsIgnoreCase(p.getCourse().getDepartment().getDeptId()))
                .map(this::mapToPacketResponseDto)
                .collect(Collectors.toList());
    }

    public DepartmentStatsDto getDepartmentStatistics(String deptId) {
        List<DepartmentPacketResponseDto> packets = getAllDepartmentPackets(deptId);

        long total = packets.size();
        long completed = packets.stream().filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count();
        long overdue = packets.stream().filter(DepartmentPacketResponseDto::isOverdue).count();
        long inProgress = total - completed;

        return DepartmentStatsDto.builder()
                .totalPackets(total)
                .completedPackets(completed)
                .overduePackets(overdue)
                .inProgressPackets(inProgress)
                .build();
    }

    public List<DepartmentPacketResponseDto> filterAndSearchPackets(
            String deptId, String query, String status, String cycleId, String lecturerId) {

        List<ExamPacket> packets = examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getDepartment() != null
                        && deptId.equalsIgnoreCase(p.getCourse().getDepartment().getDeptId()))
                .collect(Collectors.toList());

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            packets = packets.stream()
                    .filter(p -> (p.getCourse().getCourseCode() != null && p.getCourse().getCourseCode().toLowerCase().contains(q)) ||
                            (p.getCourse().getCourseName() != null && p.getCourse().getCourseName().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            packets = packets.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (cycleId != null && !cycleId.isBlank()) {
            packets = packets.stream()
                    .filter(p -> p.getAcademicCycle() != null && p.getAcademicCycle().getCycleId().equalsIgnoreCase(cycleId))
                    .collect(Collectors.toList());
        }

        if (lecturerId != null && !lecturerId.isBlank()) {
            List<String> assignedPacketIds = packetAssignmentRepository.findAll().stream()
                    .filter(pa -> pa.getUser() != null && pa.getUser().getUserId().equalsIgnoreCase(lecturerId))
                    .filter(pa -> pa.getPacket() != null)
                    .map(pa -> pa.getPacket().getPacketId())
                    .collect(Collectors.toList());

            packets = packets.stream()
                    .filter(p -> assignedPacketIds.contains(p.getPacketId()))
                    .collect(Collectors.toList());
        }

        return packets.stream().map(this::mapToPacketResponseDto).collect(Collectors.toList());
    }

    public PacketDetailDto getPacketDetails(String packetId) {
        ExamPacket packet = examPacketRepository.findById(packetId)
                .orElseThrow(() -> new RuntimeException("Exam packet not found with ID: " + packetId));

        List<PacketMovement> movements = packetMovementRepository.findAll().stream()
                .filter(pm -> pm.getPacket() != null && packetId.equalsIgnoreCase(pm.getPacket().getPacketId()))
                .collect(Collectors.toList());

        String lastUpdatedUser = "N/A";
        if (!movements.isEmpty()) {
            PacketMovement lastMovement = movements.get(movements.size() - 1);
            if (lastMovement.getFromUser() != null) {
                lastUpdatedUser = lastMovement.getFromUser().getName();
            }
        }

        List<PacketMovementDto> movementDtos = movements.stream()
                .map(m -> PacketMovementDto.builder()
                        .movementId(m.getMovementId())
                        .fromUserName(m.getFromUser() != null ? m.getFromUser().getName() : "N/A")
                        .toUserName(m.getToUser() != null ? m.getToUser().getName() : "N/A")
                        .action(m.getAction())
                        .timestamp(m.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return PacketDetailDto.builder()
                .packetId(packet.getPacketId())
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .departmentName(packet.getCourse() != null && packet.getCourse().getDepartment() != null ?
                        packet.getCourse().getDepartment().getDeptName() : "N/A")
                .cycleId(packet.getAcademicCycle() != null ? packet.getAcademicCycle().getCycleId() : "N/A")
                .status(packet.getStatus())
                .deadline(packet.getDeadline())
                .currentHolderName(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getName() : "Unassigned")
                .lastUpdatedUser(lastUpdatedUser)
                .movementHistory(movementDtos)
                .build();
    }

    public DepartmentPacketResponseDto mapToPacketResponseDto(ExamPacket packet) {
        boolean isOverdue = packet.getDeadline() != null &&
                packet.getDeadline().isBefore(LocalDate.now()) &&
                !"Completed".equalsIgnoreCase(packet.getStatus());

        return DepartmentPacketResponseDto.builder()
                .packetId(packet.getPacketId())
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .cycleId(packet.getAcademicCycle() != null ? packet.getAcademicCycle().getCycleId() : "N/A")
                .status(packet.getStatus())
                .deadline(packet.getDeadline())
                .currentHolderId(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getUserId() : null)
                .currentHolderName(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getName() : "Unassigned")
                .isOverdue(isOverdue)
                .build();
    }
}
