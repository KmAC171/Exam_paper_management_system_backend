package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.dto.PacketCourseDetailsDTO;
import com.exam_paper.backend.service.AssignedExamPacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packets")
@RequiredArgsConstructor
public class AssignedExamPacketController {

    private final AssignedExamPacketService service;

    // =========================================================
    // 1. MY PACKETS (LECTURER DASHBOARD)
    // GET /api/packets/my?lecturerId=1
    // =========================================================
    @GetMapping("/my")
    public List<AssignedPacketDTO> getMyPackets(@RequestParam Long lecturerId) {
        return service.getAssignedPackets(lecturerId);
    }

    // =========================================================
    // 2. SINGLE PACKET DETAILS (LECTURER ONLY ACCESS)
    // GET /api/packets/{packetId}/my?lecturerId=1
    // =========================================================
    @GetMapping("/{packetId}/my")
    public PacketCourseDetailsDTO getPacketById(
            @PathVariable Long packetId,
            @RequestParam Long lecturerId) {

        return service.getPacketByIdForLecturer(packetId, lecturerId);
    }
}