package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.enums.TaskType;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.PacketMovementRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerPacketService {

    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final PacketMovementRepository packetMovementRepository;
    private final UserRepository userRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public List<AssignedPacketResponseDTO> getAssignedPackets(String lecturerId) {
        Long lId = parseId(lecturerId);
        if (lId == null) {
            User user = userRepository.findByUsername(lecturerId).orElse(null);
            if (user != null) lId = user.getUserId();
        }
        if (lId == null) return List.of();

        List<PacketAssignment> assignments = packetAssignmentRepository.findByUserUserId(lId);
        List<AssignedPacketResponseDTO> list = new ArrayList<>();

        for (PacketAssignment assignment : assignments) {
            if (assignment.getPacket() != null) {
                list.add(convertToDto(assignment));
            }
        }

        // Also check directly assigned packets on ExamPacket entity
        List<ExamPacket> lecturerPackets = examPacketRepository.findByLecturerUserId(lId);
        for (ExamPacket p : lecturerPackets) {
            boolean exists = list.stream().anyMatch(dto -> dto.getPacketId().equals(String.valueOf(p.getPacketId())));
            if (!exists) {
                list.add(AssignedPacketResponseDTO.builder()
                        .packetId(String.valueOf(p.getPacketId()))
                        .courseCode(p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A")
                        .courseName(p.getCourse() != null ? p.getCourse().getCourseName() : "N/A")
                        .departmentName(p.getCourse() != null && p.getCourse().getDepartment() != null
                                ? p.getCourse().getDepartment().getDepartmentName() : "N/A")
                        .academicYear(p.getDeadline() != null ? p.getDeadline().getYear() : 2026)
                        .semester(1)
                        .deadline(p.getDeadline())
                        .status(p.getStatus() != null ? p.getStatus().getStatusName() : "PENDING")
                        .currentHolderName(p.getLecturer() != null ? p.getLecturer().getFullName() : "Unassigned")
                        .taskType(TaskType.SET_PAPER)
                        .build());
            }
        }

        return list;
    }

    private AssignedPacketResponseDTO convertToDto(PacketAssignment assignment) {
        ExamPacket packet = assignment.getPacket();
        return AssignedPacketResponseDTO.builder()
                .packetId(String.valueOf(packet.getPacketId()))
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .departmentName(packet.getCourse() != null && packet.getCourse().getDepartment() != null
                        ? packet.getCourse().getDepartment().getDepartmentName() : "N/A")
                .academicYear(packet.getDeadline() != null ? packet.getDeadline().getYear() : 2026)
                .semester(1)
                .deadline(packet.getDeadline())
                .status(packet.getStatus() != null ? packet.getStatus().getStatusName() : "PENDING")
                .currentHolderName(packet.getLecturer() != null ? packet.getLecturer().getFullName() : "Unassigned")
                .taskType(assignment.getTaskType())
                .build();
    }

    public PacketDetailsResponseDTO getPacketDetails(String packetId) {
        Long pId = parseId(packetId);
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found: " + packetId));

        return PacketDetailsResponseDTO.builder()
                .packetId(String.valueOf(packet.getPacketId()))
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .departmentName(packet.getCourse() != null && packet.getCourse().getDepartment() != null
                        ? packet.getCourse().getDepartment().getDepartmentName() : "N/A")
                .academicYear(packet.getDeadline() != null ? packet.getDeadline().getYear() : 2026)
                .semester(1)
                .deadline(packet.getDeadline())
                .status(packet.getStatus() != null ? packet.getStatus().getStatusName() : "PENDING")
                .currentHolderName(packet.getLecturer() != null ? packet.getLecturer().getFullName() : "Unassigned")
                .build();
    }

    public List<PreviousPacketResponseDTO> getPreviousPackets() {
        List<ExamPacket> packets = examPacketRepository.findAll().stream()
                .filter(p -> p.getStatus() != null &&
                        ("COMPLETED".equalsIgnoreCase(p.getStatus().getStatusName()) ||
                         "APPROVED".equalsIgnoreCase(p.getStatus().getStatusName())))
                .collect(Collectors.toList());

        return packets.stream().map(packet -> PreviousPacketResponseDTO.builder()
                .packetId(String.valueOf(packet.getPacketId()))
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .departmentName(packet.getCourse() != null && packet.getCourse().getDepartment() != null
                        ? packet.getCourse().getDepartment().getDepartmentName() : "N/A")
                .academicYear(packet.getDeadline() != null ? packet.getDeadline().getYear() : 2026)
                .semester(1)
                .status(packet.getStatus() != null ? packet.getStatus().getStatusName() : "COMPLETED")
                .deadline(packet.getDeadline())
                .currentHolderName(packet.getLecturer() != null ? packet.getLecturer().getFullName() : "Unassigned")
                .build()).collect(Collectors.toList());
    }

    public List<PacketMovementResponseDTO> getPacketMovementHistory(String packetId) {
        Long pId = parseId(packetId);
        if (pId == null) return List.of();

        List<PacketMovement> movements = packetMovementRepository.findByPacketPacketIdOrderByTimestampAsc(pId);

        return movements.stream().map(m -> PacketMovementResponseDTO.builder()
                .movementId(m.getMovementId())
                .fromUser(m.getFromUser() != null ? m.getFromUser().getFullName() : "System / Exam Branch")
                .toUser(m.getToUser() != null ? m.getToUser().getFullName() : "N/A")
                .action(m.getAction())
                .timestamp(m.getTimestamp())
                .build()).collect(Collectors.toList());
    }

    public List<ExamPacketResponseDTO> searchPackets(String keyword) {
        String kw = keyword != null ? keyword.toLowerCase() : "";
        return examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && (
                        (p.getCourse().getCourseCode() != null && p.getCourse().getCourseCode().toLowerCase().contains(kw)) ||
                        (p.getCourse().getCourseName() != null && p.getCourse().getCourseName().toLowerCase().contains(kw)) ||
                        String.valueOf(p.getPacketId()).contains(kw)))
                .map(packet -> ExamPacketResponseDTO.builder()
                        .packetId(String.valueOf(packet.getPacketId()))
                        .courseCode(packet.getCourse().getCourseCode())
                        .courseName(packet.getCourse().getCourseName())
                        .status(packet.getStatus() != null ? packet.getStatus().getStatusName() : "PENDING")
                        .build())
                .collect(Collectors.toList());
    }

    public LecturerPacketCountResponseDTO getAssignedPacketCount(String lecturerId) {
        Long lId = parseId(lecturerId);
        if (lId == null) {
            User user = userRepository.findByUsername(lecturerId).orElse(null);
            if (user != null) lId = user.getUserId();
        }
        long count = lId != null ? packetAssignmentRepository.countByUserUserId(lId) : 0;
        return new LecturerPacketCountResponseDTO(lecturerId, count);
    }
}
