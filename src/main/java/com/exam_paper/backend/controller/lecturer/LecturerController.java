package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.dto.lecturer.UpdatePacketStatusRequestDTO;
import com.exam_paper.backend.service.lecturer.LecturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerController {

    private final LecturerService lecturerService;

    /*
        Feature:
        View exam paper packets assigned
        for current lecturer

        API:
        GET
        /api/lecturer/{lecturerId}/packets
    */
    @GetMapping("/{lecturerId}/packets")
    public ResponseEntity<List<AssignedPacketResponseDTO>> getAssignedPackets(
            @PathVariable String lecturerId
    ) {
        List<AssignedPacketResponseDTO> packets = lecturerService.getAssignedPackets(lecturerId);
        return ResponseEntity.ok(packets);
    }

    @GetMapping("/packets/{packetId}")
    public ResponseEntity<PacketDetailsResponseDTO> getPacketDetails(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerService.getPacketDetails(packetId)
        );
    }

    @PostMapping("/marking")
    public ResponseEntity<String> addMarkingScripts(
            @RequestBody AddMarkingRequestDTO request
    ) {
        return ResponseEntity.ok(
                lecturerService.addMarkingScripts(request)
        );
    }
    @GetMapping("/marking/{packetId}")
    public ResponseEntity<MarkingResponseDTO> getMarkingByPacketId(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerService.getMarkingByPacketId(packetId)
        );
    }
    /*
        Access previous academic packet records

        GET:
        /api/lecturer/packets/previous
    */
    @GetMapping("/packets/previous")
    public ResponseEntity<List<PreviousPacketResponseDTO>> getPreviousPackets() {
        return ResponseEntity.ok(
                lecturerService.getPreviousPackets()
        );
    }

    @GetMapping("/packets/{packetId}/movements")
    public ResponseEntity<List<PacketMovementResponseDTO>> getPacketMovementHistory(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerService.getPacketMovementHistory(packetId)
        );
    }

    @PutMapping("/packets/{packetId}/status")
    public ResponseEntity<String> updatePacketStatus(
            @PathVariable String packetId,
            @RequestBody UpdatePacketStatusRequestDTO request
    ) {
        return ResponseEntity.ok(
                lecturerService.updatePacketStatus(
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
                lecturerService.completeTask(packetId)
        );
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @RequestBody CommentRequestDTO request
    ) {
        CommentResponseDTO response = lecturerService.addComment(
                request.getPacketId(),
                request.getUserId(),
                request.getCommentText()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{packetId}")
    public ResponseEntity<List<CommentResponseDTO>> getPacketComments(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerService.getPacketComments(packetId)
        );
    }

    @GetMapping("/packets/search")
    public ResponseEntity<List<ExamPacketResponseDTO>> searchPackets(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                lecturerService.searchPackets(keyword)
        );
    }

    @GetMapping("/{lecturerId}/assigned-packets/count")
    public ResponseEntity<LecturerPacketCountResponseDTO> getAssignedPacketCount(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getAssignedPacketCount(lecturerId)
        );
    }



    @GetMapping("/{lecturerId}/task-summary")
    public ResponseEntity<LecturerTaskSummaryResponseDTO> getTaskSummary(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getTaskSummary(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/workload-statistics")
    public ResponseEntity<LecturerWorkloadStatisticsDTO> getWorkloadStatistics(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getWorkloadStatistics(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/deadline-calendar")
    public ResponseEntity<List<LecturerDeadlineCalendarDTO>> getDeadlineCalendar(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getDeadlineCalendar(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/printing-schedules")
    public ResponseEntity<List<LecturerPrintingScheduleDTO>> getPrintingSchedules(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getPrintingSchedules(lecturerId)
        );
    }

    @GetMapping("/{userId}/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(
                lecturerService.getNotifications(userId)
        );
    }


    // Retrieves dashboard statistics for the specified lecturer,
// including active tasks, scripts to mark, completed tasks,
// overdue tasks, and upcoming deadlines.
    @GetMapping("/{lecturerId}/dashboard")
    public ResponseEntity<LecturerDashboardResponseDTO> getDashboard(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getDashboard(lecturerId)
        );
    }

    @GetMapping("/{lecturerId}/marking-process")
    public ResponseEntity<List<LecturerMarkingProcessDTO>> getMarkingProcess(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerService.getMarkingProcess(lecturerId)
        );
    }
}