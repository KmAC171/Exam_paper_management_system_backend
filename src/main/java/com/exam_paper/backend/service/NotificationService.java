package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.NotificationDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Notification;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.NotificationRepository;
import com.exam_paper.backend.repository.UserRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ExamPacketRepository packetRepository;

    public void sendNotification(
            Long userId,
            Long packetId,
            String message,
            String type,
            String notificationType
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        ExamPacket packet = null;
        if (packetId != null) {
            packet = packetRepository.findById(packetId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Packet not found"
                    ));
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setNotificationType(notificationType);
        notification.setPacket(packet);
        notification.setStatus("Unread");

        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserUserIdAndStatus(userId, "Unread")
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<NotificationDTO> getAllNotifications(Long userId) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        notification.setStatus("Read");
        notificationRepository.save(notification);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserUserIdAndStatus(userId, "Unread");
    }

    public void sendDeadlineReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<ExamPacket> packets = packetRepository.findAll().stream()
                .filter(p -> p.getDeadline().equals(tomorrow))
                .filter(p -> !("Completed".equals(p.getStatus().getStatusName())))
                .toList();

        for (ExamPacket packet : packets) {
            sendNotification(
                    packet.getLecturer().getUserId(),
                    packet.getPacketId(),
                    "Deadline approaching! Your packet " + packet.getCourse().getCourseCode() + " is due tomorrow.",
                    "Warning",
                    "DEADLINE_REMINDER"
            );
        }
    }

    public void sendOverdueAlerts() {
        LocalDate today = LocalDate.now();

        List<ExamPacket> packets = packetRepository.findAll().stream()
                .filter(p -> p.getDeadline().isBefore(today))
                .filter(p -> !("Completed".equals(p.getStatus().getStatusName())))
                .toList();

        for (ExamPacket packet : packets) {
            sendNotification(
                    packet.getLecturer().getUserId(),
                    packet.getPacketId(),
                    "ALERT: Your packet " + packet.getCourse().getCourseCode() + " is overdue!",
                    "Error",
                    "OVERDUE_ALERT"
            );
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getNotificationId(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getPacket() != null ? notification.getPacket().getPacketId() : null,
                notification.getNotificationType()
        );
    }
}