package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.NotificationResponseDTO;
import com.exam_paper.backend.entity.Notification;
import com.exam_paper.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerNotificationService {

    private final NotificationRepository notificationRepository;


    public List<NotificationResponseDTO> getNotifications(
            String userId
    ) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserUserIdOrderByCreatedAtDesc(
                                userId
                        );


        List<NotificationResponseDTO> response =
                new ArrayList<>();


        for (Notification notification : notifications) {

            response.add(

                    new NotificationResponseDTO(

                            notification.getNotificationId(),

                            notification.getMessage(),

                            notification.getType(),

                            notification.getStatus(),

                            notification.getCreatedAt()

                    )
            );
        }


        return response;
    }
}