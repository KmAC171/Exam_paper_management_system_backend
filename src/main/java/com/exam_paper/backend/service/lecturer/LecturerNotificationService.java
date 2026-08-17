package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.NotificationResponseDTO;
import com.exam_paper.backend.entity.Notification;
import com.exam_paper.backend.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerNotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getNotifications(String userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notification -> new NotificationResponseDTO(
                        notification.getNotificationId(),
                        notification.getMessage(),
                        notification.getType(),
                        notification.getStatus(),
                        notification.getCreatedAt()
                ))
                .toList();
    }


    @Transactional
    public void markAllAsRead(String userId) {

        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void markNotificationAsRead(
            String userId,
            String notificationId
    ) {
        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        // Make sure this notification belongs to this user
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException(
                    "Notification does not belong to this user"
            );
        }

        notification.setStatus("Read");

        notificationRepository.save(notification);
    }
}
