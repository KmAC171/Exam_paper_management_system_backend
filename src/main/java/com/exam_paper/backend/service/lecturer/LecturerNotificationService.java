package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.NotificationResponseDTO;
import com.exam_paper.backend.entity.Notification;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.NotificationRepository;
import com.exam_paper.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public List<NotificationResponseDTO> getNotifications(String userId) {
        Long uId = parseId(userId);
        if (uId == null) {
            User user = userRepository.findByUsername(userId).orElse(null);
            if (user != null) uId = user.getUserId();
        }
        if (uId == null) return List.of();

        List<Notification> notifications = notificationRepository.findByUserOrPacketLecturer(uId);

        return notifications.stream()
                .map(n -> new NotificationResponseDTO(
                        String.valueOf(n.getId()),
                        n.getMessage(),
                        n.getType(),
                        n.isRead() ? "Read" : "Unread",
                        n.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void markAllAsRead(String userId) {
        Long uId = parseId(userId);
        if (uId == null) {
            User user = userRepository.findByUsername(userId).orElse(null);
            if (user != null) uId = user.getUserId();
        }
        if (uId != null) {
            notificationRepository.markAllAsReadForLecturer(uId);
        }
    }

    @Transactional
    public void markNotificationAsRead(String userId, String notificationId) {
        Long nId = parseId(notificationId);
        if (nId == null) return;

        Notification notification = notificationRepository.findById(nId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
