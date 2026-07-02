package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.PacketMovementDTO;
import com.exam_paper.backend.service.PacketMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packet-movements")
@RequiredArgsConstructor
public class PacketMovementController {

    private final PacketMovementService service;

    // =========================================================
    // SUBMIT PACKET
    // POST /api/packet-movements/submit?packetId=1&lecturerId=1
    // =========================================================
    @PostMapping("/submit")
    public String submitPacket(
            @RequestParam Long packetId,
            @RequestParam Long lecturerId,
            @RequestParam(required = false) String remarks
    ) {
        service.submitPacket(packetId, lecturerId, remarks != null ? remarks : "");
        return "Packet submitted successfully";
    }

    // =========================================================
    // APPROVE PACKET
    // POST /api/packet-movements/approve?packetId=1&moderatorId=1
    // =========================================================
    @PostMapping("/approve")
    public String approvePacket(
            @RequestParam Long packetId,
            @RequestParam Long moderatorId,
            @RequestParam(required = false) String remarks
    ) {
        service.approvePacket(packetId, moderatorId, remarks != null ? remarks : "");
        return "Packet approved successfully";
    }

    // =========================================================
    // RETURN PACKET
    // POST /api/packet-movements/return?packetId=1&moderatorId=1
    // =========================================================
    @PostMapping("/return")
    public String returnPacket(
            @RequestParam Long packetId,
            @RequestParam Long moderatorId,
            @RequestParam(required = false) String remarks
    ) {
        service.returnPacket(packetId, moderatorId, remarks != null ? remarks : "");
        return "Packet returned successfully";
    }

    // =========================================================
    // GET PACKET MOVEMENT HISTORY
    // GET /api/packet-movements/history/{packetId}
    // =========================================================
    @GetMapping("/history/{packetId}")
    public List<PacketMovementDTO> getPacketMovementHistory(
            @PathVariable Long packetId
    ) {
        return service.getPacketMovementHistory(packetId);
    }
}
