package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final UserRepository userRepository;
    private final MarkingRepository markingRepository;
    private final PacketAssignmentRepository packetAssignmentRepository;

    public List<LecturerWorkloadDto> getDepartmentWorkload(String deptId) {
        List<User> departmentLecturers = userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null && deptId.equalsIgnoreCase(u.getDepartment().getDeptId()))
                .filter(u -> u.getRole() != null && "R1".equalsIgnoreCase(u.getRole().getRoleId()))
                .collect(Collectors.toList());

        return departmentLecturers.stream().map(lecturer -> {
            List<Marking> markings = markingRepository.findAll().stream()
                    .filter(m -> m.getLecturer() != null && lecturer.getUserId().equalsIgnoreCase(m.getLecturer().getUserId()))
                    .collect(Collectors.toList());

            List<PacketAssignment> assignments = packetAssignmentRepository.findByUserUserId(lecturer.getUserId());
            List<ExamPacket> assignedPackets = assignments.stream()
                    .map(PacketAssignment::getPacket)
                    .filter(Objects::nonNull)
                    .filter(packet -> packet.getCourse() != null
                            && packet.getCourse().getDepartment() != null
                            && deptId.equalsIgnoreCase(packet.getCourse().getDepartment().getDeptId()))
                    .collect(Collectors.toList());
            Set<String> uniquePacketIds = assignedPackets.stream()
                    .map(ExamPacket::getPacketId)
                    .collect(Collectors.toSet());

            long paperSetting = assignedPackets.stream()
                    .filter(packet -> "PAPER_SETTING".equals(normalizeStatus(packet.getStatus()))
                            || "PENDING".equals(normalizeStatus(packet.getStatus())))
                    .count();
            long moderating = assignedPackets.stream()
                    .filter(packet -> "PAPER_MODERATING".equals(normalizeStatus(packet.getStatus())))
                    .count();
            long marking = assignedPackets.stream()
                    .filter(packet -> "PAPER_MARKING".equals(normalizeStatus(packet.getStatus())))
                    .count();
            long secondMarking = assignedPackets.stream()
                    .filter(packet -> "SECOND_MARKING".equals(normalizeStatus(packet.getStatus())))
                    .count();
            long completed = assignedPackets.stream()
                    .filter(packet -> "COMPLETED".equals(normalizeStatus(packet.getStatus())))
                    .count();
            long overdue = assignedPackets.stream()
                    .filter(packet -> packet.getDeadline() != null
                            && packet.getDeadline().isBefore(LocalDate.now())
                            && !"COMPLETED".equals(normalizeStatus(packet.getStatus())))
                    .count();

            long totalScripts = markings.stream()
                    .mapToLong(markingItem -> markingItem.getTotalScripts() != null ? markingItem.getTotalScripts() : 0)
                    .sum();
            long markedScripts = markings.stream()
                    .mapToLong(markingItem -> markingItem.getMarkedScripts() != null ? markingItem.getMarkedScripts() : 0)
                    .sum();
            double progress = totalScripts == 0 ? 0.0 : ((double) markedScripts / totalScripts) * 100;

            return LecturerWorkloadDto.builder()
                    .lecturerId(lecturer.getUserId())
                    .lecturerName(lecturer.getName())
                    .paperSetting(paperSetting)
                    .moderating(moderating)
                    .marking(marking)
                    .secondMarking(secondMarking)
                    .completed(completed)
                    .overdue(overdue)
                    .totalAssignedPackets(uniquePacketIds.size())
                    .totalScripts(totalScripts)
                    .markedScripts(markedScripts)
                    .progressPercentage(Math.round(progress * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
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