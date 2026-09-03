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
@CrossOrigin(origins = "*")
public class LecturerPacketController {

    private final LecturerPacketService lecturerPacketService;

    @GetMapping("/{lecturerId}/packets")
    public ResponseEntity<List<AssignedPacketResponseDTO>> getAssignedPackets(@PathVariable String lecturerId) {
        List<AssignedPacketResponseDTO> packets = lecturerPacketService.getAssignedPackets(lecturerId);
        return ResponseEntity.ok(packets);
    }

    @GetMapping("/packets/{packetId}")
    public ResponseEntity<PacketDetailsResponseDTO> getPacketDetails(@PathVariable String packetId) {
        PacketDetailsResponseDTO packetDetails = lecturerPacketService.getPacketDetails(packetId);
        return ResponseEntity.ok(packetDetails);
    }

    @GetMapping("/packets/previous")
    public ResponseEntity<List<PreviousPacketResponseDTO>> getPreviousPackets() {
        List<PreviousPacketResponseDTO> previousPackets = lecturerPacketService.getPreviousPackets();
        return ResponseEntity.ok(previousPackets);
    }

    @GetMapping("/{packetId}/movements")
    public ResponseEntity<List<PacketMovementResponseDTO>> getPacketMovementHistory(@PathVariable String packetId) {
        List<PacketMovementResponseDTO> movements = lecturerPacketService.getPacketMovementHistory(packetId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/packets/search")
    public ResponseEntity<List<ExamPacketResponseDTO>> searchPackets(@RequestParam String keyword) {
        List<ExamPacketResponseDTO> packets = lecturerPacketService.searchPackets(keyword);
        return ResponseEntity.ok(packets);
    }

    @GetMapping("/{lecturerId}/assigned-packets/count")
    public ResponseEntity<LecturerPacketCountResponseDTO> getAssignedPacketCount(@PathVariable String lecturerId) {
        LecturerPacketCountResponseDTO count = lecturerPacketService.getAssignedPacketCount(lecturerId);
        return ResponseEntity.ok(count);
    }
}
