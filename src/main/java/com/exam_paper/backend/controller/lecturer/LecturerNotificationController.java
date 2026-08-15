package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.NotificationResponseDTO;
import com.exam_paper.backend.service.lecturer.LecturerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerNotificationController {

    private final LecturerNotificationService lecturerNotificationService;

    @GetMapping("/{userId}/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(
                lecturerNotificationService.getNotifications(userId)
        );
    }
}