package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.dto.PacketCourseDetailsDTO;
import com.exam_paper.backend.dto.LecturerDashboardDTO;
import com.exam_paper.backend.dto.UpdatePacketStatusDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignedExamPacketService {

    private final ExamPacketRepository repository;

    // =========================================================
    // 1. CURRENT SEMESTER PACKETS (DASHBOARD VIEW)
    // =========================================================
    public List<AssignedPacketDTO> getCurrentSemesterPackets(Long lecturerId) {

        List<ExamPacket> packets =
                repository.findByLecturerUserIdAndSemesterCurrentTrue(lecturerId);

        return packets.stream()
                .map(this::mapToDTO)
                .toList();
    }



    // =========================================================
    // 2. ALL PACKETS (HISTORY / MY PACKETS)
    // =========================================================
    public List<AssignedPacketDTO> getAllPackets(Long lecturerId) {

        List<ExamPacket> packets =
                repository.findByLecturerUserId(lecturerId);

        return packets.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =========================================================
    // 3. SINGLE PACKET DETAILS (SECURE ACCESS)
    // =========================================================
    public PacketCourseDetailsDTO getPacketByIdForLecturer(Long packetId, Long lecturerId) {

        ExamPacket packet = repository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"));

        // ACCESS CONTROL
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
                packet.getDeadline(),
                packet.getCurrentHolder() != null
                        ? packet.getCurrentHolder().getFullName()
                        : "Not Assigned"
        );
    }

    // =========================================================
    // 4. DASHBOARD SUMMARY (CURRENT SEMESTER)
    // =========================================================
    public LecturerDashboardDTO getDashboard(Long lecturerId) {

        long totalAssigned =
                repository.countByLecturerUserIdAndSemesterCurrentTrue(lecturerId);

        long completed =
                repository.countByLecturerUserIdAndStatusStatusName(
                        lecturerId, "Completed"
                );

        long overdue =
                repository.countOverduePackets(
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

    // =========================================================
    // 5. UPDATE STATUS
    // =========================================================
    public void updatePacketStatus(Long packetId, UpdatePacketStatusDTO dto) {

        ExamPacket packet = repository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"));

        packet.getStatus().setStatusName(dto.getStatusName());

        repository.save(packet);
    }

    // =========================================================
    // 6. COMMON MAPPER (BEST PRACTICE)
    // =========================================================
    private AssignedPacketDTO mapToDTO(ExamPacket p) {

        return new AssignedPacketDTO(
                p.getPacketId(),
                p.getCourse().getCourseCode(),
                p.getCourse().getCourseName(),
                p.getCourse().getDepartment().getDepartmentName(),
                p.getStatus().getStatusName(),
                p.getDeadline()
        );
    }

    public List<AssignedPacketDTO> searchByCourseCode(
            Long lecturerId,
            String courseCode
    ) {

        List<ExamPacket> packets =
                repository.findByLecturerUserIdAndCourseCourseCodeContainingIgnoreCase(
                        lecturerId,
                        courseCode
                );

        return packets.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> searchByCourseName(
            Long lecturerId,
            String courseName
    ) {

        List<ExamPacket> packets =
                repository.findByLecturerUserIdAndCourseCourseNameContainingIgnoreCase(
                        lecturerId,
                        courseName
                );

        return packets.stream()
                .map(this::mapToDTO)
                .toList();
    }
}