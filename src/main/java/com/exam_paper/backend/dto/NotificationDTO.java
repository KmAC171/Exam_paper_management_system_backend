package com.exam_paper.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long notificationId;
    private String message;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private Long packetId;
    private String notificationType;
}