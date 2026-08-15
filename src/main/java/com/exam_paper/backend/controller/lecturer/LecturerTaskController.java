package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.service.lecturer.LecturerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerTaskController {

    private final LecturerTaskService lecturerTaskService;

    @PutMapping("/packets/{packetId}/status")
    public ResponseEntity<String> updatePacketStatus(
            @PathVariable String packetId,
            @RequestBody UpdatePacketStatusRequestDTO request
    ) {
        return ResponseEntity.ok(
                lecturerTaskService.updatePacketStatus(
                        packetId,
                        request
                )
        );
    }

    @PutMapping("/tasks/{packetId}/complete")
    public ResponseEntity<CompleteTaskResponseDTO> completeTask(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerTaskService.completeTask(packetId)
        );
    }

    @GetMapping("/{lecturerId}/task-summary")
    public ResponseEntity<LecturerTaskSummaryResponseDTO> getTaskSummary(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerTaskService.getTaskSummary(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/workload-statistics")
    public ResponseEntity<LecturerWorkloadStatisticsDTO> getWorkloadStatistics(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerTaskService.getWorkloadStatistics(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/deadline-calendar")
    public ResponseEntity<List<LecturerDeadlineCalendarDTO>> getDeadlineCalendar(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerTaskService.getDeadlineCalendar(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/printing-schedules")
    public ResponseEntity<List<LecturerPrintingScheduleDTO>> getPrintingSchedules(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerTaskService.getPrintingSchedules(lecturerId)
        );
    }
}