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
@CrossOrigin(origins = "*")
public class LecturerTaskController {

    private final LecturerTaskService lecturerTaskService;

    @GetMapping("/{lecturerId}/task-summary")
    public ResponseEntity<LecturerTaskSummaryResponseDTO> getTaskSummary(@PathVariable String lecturerId) {
        LecturerTaskSummaryResponseDTO taskSummary = lecturerTaskService.getTaskSummary(lecturerId);
        return ResponseEntity.ok(taskSummary);
    }

    @GetMapping("/{lecturerId}/workload-statistics")
    public ResponseEntity<LecturerWorkloadStatisticsDTO> getWorkloadStatistics(@PathVariable String lecturerId) {
        LecturerWorkloadStatisticsDTO statistics = lecturerTaskService.getWorkloadStatistics(lecturerId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{lecturerId}/deadline-calendar")
    public ResponseEntity<List<LecturerDeadlineCalendarDTO>> getDeadlineCalendar(@PathVariable String lecturerId) {
        List<LecturerDeadlineCalendarDTO> deadlineCalendar = lecturerTaskService.getDeadlineCalendar(lecturerId);
        return ResponseEntity.ok(deadlineCalendar);
    }

    @GetMapping("/{lecturerId}/printing-schedules")
    public ResponseEntity<List<LecturerPrintingScheduleDTO>> getPrintingSchedules(@PathVariable String lecturerId) {
        List<LecturerPrintingScheduleDTO> schedules = lecturerTaskService.getPrintingSchedules(lecturerId);
        return ResponseEntity.ok(schedules);
    }

    @PutMapping("/packets/{packetId}/status")
    public ResponseEntity<String> updatePacketStatus(
            @PathVariable String packetId,
            @RequestBody UpdatePacketStatusRequestDTO request
    ) {
        String response = lecturerTaskService.updatePacketStatus(packetId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{packetId}/complete")
    public ResponseEntity<CompleteTaskResponseDTO> completeTask(@PathVariable String packetId) {
        CompleteTaskResponseDTO response = lecturerTaskService.completeTask(packetId);
        return ResponseEntity.ok(response);
    }
}
