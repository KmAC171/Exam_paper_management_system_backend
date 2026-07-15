package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.NotificationDTO;
import com.exam_paper.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // GET /api/notifications/unread?userId=1
    // =========================================================
    @GetMapping("/unread")
    public List<NotificationDTO> getUnreadNotifications(
            @RequestParam Long userId
    ) {
        return service.getUnreadNotifications(userId);
    }

    // =========================================================
    // GET ALL NOTIFICATIONS
    // GET /api/notifications?userId=1
    // =========================================================
    @GetMapping
    public List<NotificationDTO> getAllNotifications(
            @RequestParam Long userId
    ) {
        return service.getAllNotifications(userId);
    }

    // =========================================================
    // MARK AS READ
    // PATCH /api/notifications/{notificationId}/read
    // =========================================================
    @PatchMapping("/{notificationId}/read")
    public String markAsRead(
            @PathVariable Long notificationId
    ) {
        service.markAsRead(notificationId);
        return "Notification marked as read";
    }

    // =========================================================
    // GET UNREAD COUNT
    // GET /api/notifications/count/unread?userId=1
    // =========================================================
    @GetMapping("/count/unread")
    public long getUnreadCount(
            @RequestParam Long userId
    ) {
        return service.getUnreadCount(userId);
    }
}