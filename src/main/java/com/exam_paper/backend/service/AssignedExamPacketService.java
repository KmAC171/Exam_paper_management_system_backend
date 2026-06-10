package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.dto.PacketCourseDetailsDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.exam_paper.backend.dto.LecturerDashboardDTO;


import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignedExamPacketService {

    private final ExamPacketRepository repository;

    // =========================================================
    // 1. GET ALL PACKETS ASSIGNED TO LECTURER
    // =========================================================
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

    // =========================================================
    // 2. GET SINGLE PACKET (LECTURER ACCESS ONLY)
    // =========================================================
    public PacketCourseDetailsDTO getPacketByIdForLecturer(Long packetId, Long lecturerId) {

        ExamPacket packet = repository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"));

        // 🔒 ACCESS CONTROL (only assigned lecturer)
        if (!packet.getLecturer().getUserId().equals(lecturerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this packet"
            );
        }

        return new PacketCourseDetailsDTO(
                packet.getPacketId(),
                packet.getCourse().getCourseCode(),
                packet.getCourse().getCourseName(),
                packet.getCourse().getDepartment().getDepartmentName(),
                packet.getStatus().getStatusName(),
                packet.getDeadline()
        );
    }


    // =========================================================
    // 2. Dash Board view
    // =========================================================

    public LecturerDashboardDTO getDashboard(Long lecturerId) {

        long totalAssigned = repository.countByLecturerUserId(lecturerId);

        long completed = repository.countByLecturerUserIdAndStatusStatusName(
                lecturerId, "Completed"
        );

        long overdue = repository.countByLecturerUserIdAndDeadlineBeforeAndStatusStatusNameNot(
                lecturerId,
                LocalDate.now(),
                "Completed"
        );

        long pending = totalAssigned - completed - overdue;

        return new LecturerDashboardDTO(
                totalAssigned,
                pending,
                completed,
                overdue
        );
    }
}