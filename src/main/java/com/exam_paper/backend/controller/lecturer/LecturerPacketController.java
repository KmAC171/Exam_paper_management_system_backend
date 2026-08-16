package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.service.lecturer.LecturerPacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerPacketController {

    private final LecturerPacketService lecturerPacketService;

    @GetMapping("/{lecturerId}/packets")
    public ResponseEntity<List<AssignedPacketResponseDTO>> getAssignedPackets(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerPacketService.getAssignedPackets(lecturerId)
        );
    }

    @GetMapping("/packets/{packetId}")
    public ResponseEntity<PacketDetailsResponseDTO> getPacketDetails(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerPacketService.getPacketDetails(packetId)
        );
    }

    @GetMapping("/packets/previous")
    public ResponseEntity<List<PreviousPacketResponseDTO>> getPreviousPackets() {

        return ResponseEntity.ok(
                lecturerPacketService.getPreviousPackets()
        );
    }

    @GetMapping("/packets/{packetId}/movements")
    public ResponseEntity<List<PacketMovementResponseDTO>> getPacketMovementHistory(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerPacketService.getPacketMovementHistory(packetId)
        );
    }

    @GetMapping("/packets/search")
    public ResponseEntity<List<ExamPacketResponseDTO>> searchPackets(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                lecturerPacketService.searchPackets(keyword)
        );
    }

    @GetMapping("/{lecturerId}/assigned-packets/count")
    public ResponseEntity<LecturerPacketCountResponseDTO> getAssignedPacketCount(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerPacketService.getAssignedPacketCount(lecturerId)
        );
    }
}