package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.User;
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

    public List<LecturerWorkloadDto> getDepartmentWorkload(String deptId) {
        List<User> departmentLecturers = userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null && deptId.equalsIgnoreCase(u.getDepartment().getDeptId()))
                .filter(u -> u.getRole() != null && "R1".equalsIgnoreCase(u.getRole().getRoleId()))
                .collect(Collectors.toList());

        return departmentLecturers.stream().map(lecturer -> {
            List<Marking> markings = markingRepository.findAll().stream()
                    .filter(m -> m.getLecturer() != null && lecturer.getUserId().equalsIgnoreCase(m.getLecturer().getUserId()))
                    .collect(Collectors.toList());

            long totalAssignedPackets = packetAssignmentRepository.findAll().stream()
                    .filter(pa -> pa.getUser() != null && lecturer.getUserId().equalsIgnoreCase(pa.getUser().getUserId()))
                    .count();

            long totalScripts = markings.stream().mapToLong(Marking::getTotalScripts).sum();
            long markedScripts = markings.stream().mapToLong(Marking::getMarkedScripts).sum();
            double progress = totalScripts == 0 ? 0.0 : ((double) markedScripts / totalScripts) * 100;

            return LecturerWorkloadDto.builder()
                    .lecturerId(lecturer.getUserId())
                    .lecturerName(lecturer.getName())
                    .totalAssignedPackets(totalAssignedPackets)
                    .totalScripts(totalScripts)
                    .markedScripts(markedScripts)
                    .progressPercentage(Math.round(progress * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }
}