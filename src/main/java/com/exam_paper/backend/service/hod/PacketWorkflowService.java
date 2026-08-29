package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
        List<DepartmentPacketResponseDto> packets = getAllDepartmentPackets(deptId);

        if (query != null && !query.isBlank()) {
            packets = packets.stream()
                    .filter(p -> containsIgnoreCase(p.getPacketId(), query)
                            || containsIgnoreCase(p.getCourseCode(), query)
                            || containsIgnoreCase(p.getCourseName(), query)
                            || containsIgnoreCase(p.getCurrentHolderName(), query)
                            || containsIgnoreCase(p.getLecturerName(), query)
                            || containsIgnoreCase(p.getModeratorName(), query))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            packets = packets.stream()
                    .filter(p -> containsIgnoreCase(p.getStatus(), status)
                            || containsIgnoreCase(p.getStage(), status))
                    .collect(Collectors.toList());
        }

        if (cycleId != null && !cycleId.isBlank()) {
            packets = packets.stream()
                    .filter(p -> containsIgnoreCase(p.getCycleId(), cycleId)
                            || containsIgnoreCase(p.getAcademicCycle(), cycleId))
                    .collect(Collectors.toList());
        }

        if (lecturerId != null && !lecturerId.isBlank()) {
            packets = packets.stream()
                    .filter(p -> containsIgnoreCase(p.getLecturerId(), lecturerId))
                    .collect(Collectors.toList());
        }

        return packets;
    }

    public PacketDetailDto getPacketDetails(String packetId) {
        ExamPacket packet = examPacketRepository.findById(packetId)
                .orElseThrow(() -> new RuntimeException("Exam packet not found with ID: " + packetId));

        List<PacketMovement> movements = packetMovementRepository.findByPacketPacketIdOrderByTimestampDesc(packetId);

        String lastUpdatedUser = "N/A";
        if (!movements.isEmpty()) {
            PacketMovement lastMovement = movements.get(0);
            lastUpdatedUser = Optional.ofNullable(lastMovement.getFromUser())
                    .map(user -> user.getName())
                    .orElseGet(() -> Optional.ofNullable(lastMovement.getToUser())
                            .map(user -> user.getName())
                            .orElse("N/A"));
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
        List<PacketAssignment> assignments = packetAssignmentRepository.findByPacketPacketId(packet.getPacketId());
        Optional<PacketAssignment> lecturerAssignment = assignments.stream()
                .filter(pa -> pa.getUser() != null && pa.getUser().getRole() != null)
                .filter(pa -> "R1".equalsIgnoreCase(pa.getUser().getRole().getRoleId())
                        || containsIgnoreCase(pa.getUser().getRole().getRoleName(), "lecturer"))
                .findFirst();
        Optional<PacketAssignment> moderatorAssignment = assignments.stream()
                .filter(pa -> containsIgnoreCase(pa.getAssignedRole(), "moderat")
                        || (pa.getUser() != null && pa.getUser().getRole() != null
                        && containsIgnoreCase(pa.getUser().getRole().getRoleName(), "moderat")))
                .findFirst();

        List<PacketMovement> movements = packetMovementRepository.findByPacketPacketIdOrderByTimestampDesc(packet.getPacketId());
        LocalDateTime lastUpdatedTime = movements.isEmpty() ? null : movements.get(0).getTimestamp();
        String lastUpdatedUser = movements.isEmpty() ? "N/A"
                : Optional.ofNullable(movements.get(0).getFromUser())
                .map(user -> user.getName())
                .orElseGet(() -> Optional.ofNullable(movements.get(0).getToUser())
                        .map(user -> user.getName())
                        .orElse("N/A"));

        Marking marking = packet.getMarking();
        Long totalPapers = marking != null && marking.getTotalScripts() != null
                ? marking.getTotalScripts().longValue() : null;
        Long papersToMark = marking != null && marking.getTotalScripts() != null && marking.getMarkedScripts() != null
                ? Long.valueOf(Math.max(marking.getTotalScripts() - marking.getMarkedScripts(), 0)) : null;
        String stage = normalizeStatus(packet.getStatus());
        String cycle = packet.getAcademicCycle() != null ? packet.getAcademicCycle().getCycleId() : "N/A";

        boolean isOverdue = packet.getDeadline() != null &&
                packet.getDeadline().isBefore(LocalDate.now()) &&
                !"COMPLETED".equalsIgnoreCase(stage);

        return DepartmentPacketResponseDto.builder()
                .packetId(packet.getPacketId())
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .cycleId(cycle)
                .academicCycle(cycle)
                .stage(stage)
                .status(packet.getStatus())
                .deadline(packet.getDeadline())
                .currentHolderId(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getUserId() : null)
                .currentHolder(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getName() : "Unassigned")
                .currentHolderName(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getName() : "Unassigned")
                .lecturerId(lecturerAssignment.map(assignment -> assignment.getUser().getUserId()).orElse(null))
                .lecturerName(lecturerAssignment.map(assignment -> assignment.getUser().getName()).orElse("Unassigned"))
                .moderatorName(moderatorAssignment.map(assignment -> assignment.getUser().getName()).orElse("Unassigned"))
                .totalPapers(totalPapers)
                .papersToMark(papersToMark)
                .lastUpdatedTime(lastUpdatedTime)
                .lastUpdatedUser(lastUpdatedUser)
                .isOverdue(isOverdue)
                .build();
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && query != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNKNOWN";
        }
        return status.trim()
                .replace(' ', '_')
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }
}