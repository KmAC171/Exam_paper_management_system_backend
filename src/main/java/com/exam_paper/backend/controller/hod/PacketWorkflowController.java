package com.exam_paper.backend.controller.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.dto.hod.DepartmentStatsDto;
import com.exam_paper.backend.dto.hod.PacketDetailDto;
import com.exam_paper.backend.service.hod.PacketWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PacketWorkflowController {

    private final PacketWorkflowService packetWorkflowService;

    @GetMapping("/department/{deptId}/packets")
    public ResponseEntity<List<DepartmentPacketResponseDto>> getAllDepartmentPackets(@PathVariable String deptId) {
        return ResponseEntity.ok(packetWorkflowService.getAllDepartmentPackets(deptId));
    }

    @GetMapping("/department/{deptId}/statistics")
    public ResponseEntity<DepartmentStatsDto> getDepartmentStatistics(@PathVariable String deptId) {
        return ResponseEntity.ok(packetWorkflowService.getDepartmentStatistics(deptId));
    }

    @GetMapping("/department/{deptId}/packets/search")
    public ResponseEntity<List<DepartmentPacketResponseDto>> searchPackets(
            @PathVariable String deptId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cycleId,
            @RequestParam(required = false) String lecturerId) {
        return ResponseEntity.ok(packetWorkflowService.filterAndSearchPackets(deptId, query, status, cycleId, lecturerId));
    }

    @GetMapping("/packet/{packetId}")
    public ResponseEntity<PacketDetailDto> getPacketDetails(@PathVariable String packetId) {
        return ResponseEntity.ok(packetWorkflowService.getPacketDetails(packetId));
    }
}