package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final UserRepository userRepository;
    private final MarkingRepository markingRepository;
    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public List<LecturerWorkloadDto> getDepartmentWorkload(String deptId) {
        Long dId = parseId(deptId);

        List<User> departmentLecturers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ROLE_USER)
                .filter(u -> {
                    if (dId == null || deptId.isBlank() || "ALL".equalsIgnoreCase(deptId)) return true;
                    if (u.getDepartment() != null) {
                        return u.getDepartment().getDepartmentId().equals(dId) ||
                                (u.getDepartment().getDepartmentName() != null &&
                                 u.getDepartment().getDepartmentName().equalsIgnoreCase(deptId));
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return departmentLecturers.stream().map(lecturer -> {
            List<Marking> markings = markingRepository.findByLecturerUserId(lecturer.getUserId());

            long totalAssignedPackets = packetAssignmentRepository.countByUserUserId(lecturer.getUserId());
            if (totalAssignedPackets == 0) {
                totalAssignedPackets = examPacketRepository.findByLecturerUserId(lecturer.getUserId()).size();
            }

            long totalScripts = markings.stream()
                    .mapToLong(m -> m.getTotalScripts() != null ? m.getTotalScripts() : 0)
                    .sum();
            long markedScripts = markings.stream()
                    .mapToLong(m -> m.getMarkedScripts() != null ? m.getMarkedScripts() : 0)
                    .sum();
            double progress = totalScripts == 0 ? 0.0 : ((double) markedScripts / totalScripts) * 100;

            return LecturerWorkloadDto.builder()
                    .lecturerId(String.valueOf(lecturer.getUserId()))
                    .lecturerName(lecturer.getFullName())
                    .totalAssignedPackets(totalAssignedPackets)
                    .totalScripts(totalScripts)
                    .markedScripts(markedScripts)
                    .progressPercentage(Math.round(progress * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }
}
