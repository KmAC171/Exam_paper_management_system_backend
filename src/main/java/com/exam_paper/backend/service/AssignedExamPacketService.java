package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.dto.LecturerDashboardDTO;
import com.exam_paper.backend.dto.PacketCourseDetailsDTO;
import com.exam_paper.backend.dto.UpdatePacketStatusDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketStatusRepository;
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
    private final PacketStatusRepository statusRepository;

    public List<AssignedPacketDTO> getCurrentSemesterPackets(Long lecturerId) {
        return repository.findByLecturerUserIdAndSemesterCurrentTrue(lecturerId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> getAllPackets(Long lecturerId) {
        return repository.findByLecturerUserId(lecturerId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public PacketCourseDetailsDTO getPacketByIdForLecturer(Long packetId, Long lecturerId) {
        ExamPacket packet = repository.findById(packetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Packet not found"));

        if (packet.getLecturer() == null || !packet.getLecturer().getUserId().equals(lecturerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this packet");
        }

        return new PacketCourseDetailsDTO(
                packet.getPacketId(),
                packet.getCourse().getCourseCode(),
                packet.getCourse().getCourseName(),
                packet.getCourse().getDepartment().getDepartmentName(),
                packet.getStatus().getStatusName(),
                packet.getDeadline(),
                packet.getCurrentHolder() != null ? packet.getCurrentHolder().getFullName() : "Not Assigned"
        );
    }

    public LecturerDashboardDTO getDashboard(Long lecturerId) {
        long totalAssigned = repository.countByLecturerUserIdAndSemesterCurrentTrue(lecturerId);
        long completed = repository.countByLecturerUserIdAndStatusStatusName(lecturerId, "Completed");
        long overdue = repository.countOverduePackets(lecturerId, LocalDate.now(), "Completed");
        long pending = Math.max(totalAssigned - completed - overdue, 0);

        return new LecturerDashboardDTO(totalAssigned, pending, completed, overdue);
    }

    public void updatePacketStatus(Long packetId, UpdatePacketStatusDTO dto) {
        ExamPacket packet = repository.findById(packetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Packet not found"));

        PacketStatus status = statusRepository.findByStatusName(dto.getStatusName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        packet.setStatus(status);
        repository.save(packet);
    }

    public List<AssignedPacketDTO> searchByCourseCode(Long lecturerId, String courseCode) {
        return repository.findByLecturerUserIdAndCourseCourseCodeContainingIgnoreCase(lecturerId, courseCode)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> searchByCourseName(Long lecturerId, String courseName) {
        return repository.findByLecturerUserIdAndCourseCourseNameContainingIgnoreCase(lecturerId, courseName)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> filterByStatus(Long lecturerId, String statusName) {
        return repository.findByLecturerUserIdAndStatusStatusNameIgnoreCase(lecturerId, statusName)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> filterByDeadline(Long lecturerId, LocalDate deadline) {
        return repository.findByLecturerUserIdAndDeadline(lecturerId, deadline)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> getPendingAndCompletedPackets(Long lecturerId) {
        return repository.findByLecturerUserIdAndStatusStatusNameIn(lecturerId, List.of("Pending", "Completed"))
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<AssignedPacketDTO> getOverduePackets(Long lecturerId) {
        return repository.findOverduePackets(lecturerId, LocalDate.now(), "Completed")
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

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
}