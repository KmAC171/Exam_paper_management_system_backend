package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.NotificationDTO;
import com.exam_paper.backend.entity.Notification;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.NotificationRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationDTO> getNotifications(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications;
        switch (role) {
            case "ROLE_ADMIN", "ROLE_GUEST" ->
                    notifications = notificationRepository.findAllByOrderByCreatedAtDesc();
            case "ROLE_USER" ->
                    notifications = notificationRepository.findByUserOrPacketLecturer(user.getUserId());
            case "ROLE_MODERATOR" ->
                    notifications = notificationRepository.findByUserOrPacketModerator(user.getUserId());
            default -> notifications = List.of();
        }

        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getCourseCode(),
                n.isRead(),
                n.isUrgent(),
                timeAgo(n.getCreatedAt()),
                formatTime(n.getCreatedAt())
        );
    }

    private String timeAgo(LocalDateTime dt) {
        if (dt == null) return "";
        long minutes = ChronoUnit.MINUTES.between(dt, LocalDateTime.now());
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";
        long hours = ChronoUnit.HOURS.between(dt, LocalDateTime.now());
        if (hours < 24) return hours + " hours ago";
        long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
        if (days == 1) return "Yesterday, " + dt.format(DateTimeFormatter.ofPattern("h:mm a"));
        return days + " days ago";
    }

    private String formatTime(LocalDateTime dt) {
        if (dt == null) return "";
        long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
        if (days == 0) return dt.format(DateTimeFormatter.ofPattern("h:mm a"));
        if (days == 1) return "Yesterday, " + dt.format(DateTimeFormatter.ofPattern("h:mm a"));
        return dt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"));
    }
}