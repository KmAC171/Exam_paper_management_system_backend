package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacketWorkflowService {

    private final ExamPacketRepository examPacketRepository;
    private final PacketMovementRepository packetMovementRepository;
    private final PacketAssignmentRepository packetAssignmentRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean matchDepartment(ExamPacket p, String deptId) {
        if (p.getCourse() == null || p.getCourse().getDepartment() == null) return true;
        if (deptId == null || deptId.isBlank() || "ALL".equalsIgnoreCase(deptId)) return true;

        Long dId = parseId(deptId);
        if (dId != null && p.getCourse().getDepartment().getDepartmentId().equals(dId)) {
            return true;
        }
        return p.getCourse().getDepartment().getDepartmentName() != null &&
                p.getCourse().getDepartment().getDepartmentName().equalsIgnoreCase(deptId);
    }

    public List<DepartmentPacketResponseDto> getAllDepartmentPackets(String deptId) {
        return examPacketRepository.findAll().stream()
                .filter(p -> matchDepartment(p, deptId))
                .map(this::mapToPacketResponseDto)
                .collect(Collectors.toList());
    }

    public DepartmentStatsDto getDepartmentStatistics(String deptId) {
        List<DepartmentPacketResponseDto> packets = getAllDepartmentPackets(deptId);

        long total = packets.size();
        long completed = packets.stream().filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()) || "APPROVED".equalsIgnoreCase(p.getStatus())).count();
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
                .filter(p -> matchDepartment(p, deptId))
                .collect(Collectors.toList());

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            packets = packets.stream()
                    .filter(p -> (p.getCourse() != null && p.getCourse().getCourseCode() != null && p.getCourse().getCourseCode().toLowerCase().contains(q)) ||
                            (p.getCourse() != null && p.getCourse().getCourseName() != null && p.getCourse().getCourseName().toLowerCase().contains(q)) ||
                            String.valueOf(p.getPacketId()).contains(q))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            packets = packets.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().getStatusName() != null &&
                            p.getStatus().getStatusName().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (lecturerId != null && !lecturerId.isBlank()) {
            Long lId = parseId(lecturerId);
            packets = packets.stream()
                    .filter(p -> (lId != null && p.getLecturer() != null && p.getLecturer().getUserId().equals(lId)) ||
                            (p.getLecturer() != null && p.getLecturer().getUsername().equalsIgnoreCase(lecturerId)))
                    .collect(Collectors.toList());
        }

        return packets.stream().map(this::mapToPacketResponseDto).collect(Collectors.toList());
    }

    public PacketDetailDto getPacketDetails(String packetId) {
        Long pId = parseId(packetId);
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Exam packet not found with ID: " + packetId));

        List<PacketMovement> movements = packetMovementRepository.findByPacketPacketIdOrderByTimestampAsc(packet.getPacketId());

        String lastUpdatedUser = "N/A";
        if (!movements.isEmpty()) {
            PacketMovement lastMovement = movements.get(movements.size() - 1);
            if (lastMovement.getFromUser() != null) {
                lastUpdatedUser = lastMovement.getFromUser().getFullName();
            }
        }

        List<PacketMovementDto> movementDtos = movements.stream()
                .map(m -> PacketMovementDto.builder()
                        .movementId(m.getMovementId())
                        .fromUserName(m.getFromUser() != null ? m.getFromUser().getFullName() : "N/A")
                        .toUserName(m.getToUser() != null ? m.getToUser().getFullName() : "N/A")
                        .action(m.getAction())
                        .timestamp(m.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        String statusStr = packet.getStatus() != null ? packet.getStatus().getStatusName() : "PENDING";
        String holderName = packet.getLecturer() != null ? packet.getLecturer().getFullName() : "Unassigned";

        return PacketDetailDto.builder()
                .packetId(String.valueOf(packet.getPacketId()))
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .departmentName(packet.getCourse() != null && packet.getCourse().getDepartment() != null ?
                        packet.getCourse().getDepartment().getDepartmentName() : "N/A")
                .cycleId(packet.getDeadline() != null ? String.valueOf(packet.getDeadline().getYear()) : "2026")
                .status(statusStr)
                .deadline(packet.getDeadline())
                .currentHolderName(holderName)
                .lastUpdatedUser(lastUpdatedUser)
                .movementHistory(movementDtos)
                .build();
    }

    public DepartmentPacketResponseDto mapToPacketResponseDto(ExamPacket packet) {
        String statusStr = packet.getStatus() != null ? packet.getStatus().getStatusName() : "PENDING";
        boolean isOverdue = packet.getDeadline() != null &&
                packet.getDeadline().isBefore(LocalDate.now()) &&
                !"COMPLETED".equalsIgnoreCase(statusStr) &&
                !"APPROVED".equalsIgnoreCase(statusStr);

        return DepartmentPacketResponseDto.builder()
                .packetId(String.valueOf(packet.getPacketId()))
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .cycleId(packet.getDeadline() != null ? String.valueOf(packet.getDeadline().getYear()) : "2026")
                .status(statusStr)
                .deadline(packet.getDeadline())
                .currentHolderId(packet.getLecturer() != null ? String.valueOf(packet.getLecturer().getUserId()) : null)
                .currentHolderName(packet.getLecturer() != null ? packet.getLecturer().getFullName() : "Unassigned")
                .isOverdue(isOverdue)
                .build();
    }
}
