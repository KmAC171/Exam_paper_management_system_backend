package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignedExamPacketService {

    private final ExamPacketRepository repository;

    public List<AssignedPacketDTO> getAssignedPackets(Long lecturerId) {

        List<ExamPacket> packets = repository.findByLecturerUserId(lecturerId);

        return packets.stream()
                .map(p -> new AssignedPacketDTO(
                        p.getPacketId(),
                        p.getCourse().getCourseCode(),
                        p.getCourse().getCourseName(),
                        p.getCourse().getDepartment().getDepartmentName(),
                        p.getStatus().getStatusName(),
                        p.getDeadline()
                ))
                .toList();
    }
}