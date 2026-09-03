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
@CrossOrigin(origins = "*")
public class LecturerNotificationController {

    private final LecturerNotificationService notificationService;

    @GetMapping("/{userId}/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(@PathVariable String userId) {
        List<NotificationResponseDTO> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{userId}/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String userId,
            @PathVariable String notificationId
    ) {
        notificationService.markNotificationAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
