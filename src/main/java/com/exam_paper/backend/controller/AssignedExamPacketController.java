package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.dto.LecturerDashboardDTO;
import com.exam_paper.backend.dto.PacketCourseDetailsDTO;
import com.exam_paper.backend.dto.UpdatePacketStatusDTO;
import com.exam_paper.backend.service.AssignedExamPacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/packets")
@RequiredArgsConstructor
public class AssignedExamPacketController {

    private final AssignedExamPacketService service;

    // =========================================================
    // 1. CURRENT SEMESTER PACKETS (DASHBOARD VIEW)
    // GET /api/packets/dashboard/current?lecturerId=1
    // =========================================================
    @GetMapping("/dashboard/current")
    public List<AssignedPacketDTO> getCurrentSemesterPackets(
            @RequestParam Long lecturerId
    ) {
        return service.getCurrentSemesterPackets(lecturerId);
    }

    // =========================================================
    // 2. ALL PACKETS (HISTORY / MY PACKETS)
    // GET /api/packets/my?lecturerId=1
    // =========================================================
    @GetMapping("/my")
    public List<AssignedPacketDTO> getMyPackets(
            @RequestParam Long lecturerId
    ) {
        return service.getAllPackets(lecturerId);
    }

    // =========================================================
    // 3. SINGLE PACKET DETAILS
    // GET /api/packets/{packetId}/my?lecturerId=1
    // =========================================================
    @GetMapping("/{packetId}/my")
    public PacketCourseDetailsDTO getPacketById(
            @PathVariable Long packetId,
            @RequestParam Long lecturerId
    ) {
        return service.getPacketByIdForLecturer(packetId, lecturerId);
    }

    // =========================================================
    // 4. DASHBOARD SUMMARY
    // GET /api/packets/dashboard?lecturerId=1
    // =========================================================
    @GetMapping("/dashboard")
    public LecturerDashboardDTO getDashboard(
            @RequestParam Long lecturerId
    ) {
        return service.getDashboard(lecturerId);
    }

    // =========================================================
    // 5. UPDATE PACKET STATUS
    // PATCH /api/packets/{id}/status
    // =========================================================
    @PatchMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestBody UpdatePacketStatusDTO dto
    ) {
        service.updatePacketStatus(id, dto);
        return "Packet status updated successfully";
    }

    // =========================================================
// SEARCH BY COURSE CODE
// GET /api/packets/search/course-code?lecturerId=1&courseCode=CS
// =========================================================
    @GetMapping("/search/course-code")
    public List<AssignedPacketDTO> searchByCourseCode(
            @RequestParam Long lecturerId,
            @RequestParam String courseCode
    ) {
        return service.searchByCourseCode(
                lecturerId,
                courseCode
        );
    }

    // =========================================================
// SEARCH BY COURSE NAME
// GET /api/packets/search/course-name?lecturerId=1&courseName=Programming
// =========================================================
    @GetMapping("/search/course-name")
    public List<AssignedPacketDTO> searchByCourseName(
            @RequestParam Long lecturerId,
            @RequestParam String courseName
    ) {
        return service.searchByCourseName(
                lecturerId,
                courseName
        );
    }

    // =========================================================
// FILTER BY STATUS
// GET /api/packets/filter/status?lecturerId=1&status=Pending
// =========================================================
    @GetMapping("/filter/status")
    public List<AssignedPacketDTO> filterByStatus(
            @RequestParam Long lecturerId,
            @RequestParam String status
    ) {
        return service.filterByStatus(
                lecturerId,
                status
        );
    }

    // =========================================================
// FILTER BY DEADLINE
// GET /api/packets/filter/deadline?lecturerId=1&deadline=2026-03-01
// =========================================================
    @GetMapping("/filter/deadline")
    public List<AssignedPacketDTO> filterByDeadline(
            @RequestParam Long lecturerId,
            @RequestParam String deadline
    ) {

        return service.filterByDeadline(
                lecturerId,
                LocalDate.parse(deadline)
        );
    }

    // =========================================================
// VIEW PENDING + COMPLETED PACKETS
// GET /api/packets/status/summary?lecturerId=1
// =========================================================
    @GetMapping("/status/summary")
    public List<AssignedPacketDTO> getPendingAndCompletedPackets(
            @RequestParam Long lecturerId
    ) {
        return service.getPendingAndCompletedPackets(lecturerId);
    }

    // =========================================================
// VIEW OVERDUE PACKETS
// GET /api/packets/overdue?lecturerId=1
// =========================================================
    @GetMapping("/overdue")
    public List<AssignedPacketDTO> getOverduePackets(
            @RequestParam Long lecturerId
    ) {
        return service.getOverduePackets(lecturerId);
    }


}