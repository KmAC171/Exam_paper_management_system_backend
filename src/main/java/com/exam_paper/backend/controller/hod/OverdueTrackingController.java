package com.exam_paper.backend.controller.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.service.hod.OverdueTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
public class OverdueTrackingController {

    private final OverdueTrackingService overdueTrackingService;

    @GetMapping("/department/{deptId}/previous-records")
    public ResponseEntity<List<DepartmentPacketResponseDto>> getPreviousRecords(@PathVariable String deptId) {
        return ResponseEntity.ok(overdueTrackingService.getPreviousCycleRecords(deptId));
    }

    @GetMapping("/department/{deptId}/overdue")
    public ResponseEntity<List<DepartmentPacketResponseDto>> getOverduePackets(@PathVariable String deptId) {
        return ResponseEntity.ok(overdueTrackingService.getOverduePackets(deptId));
    }
}
