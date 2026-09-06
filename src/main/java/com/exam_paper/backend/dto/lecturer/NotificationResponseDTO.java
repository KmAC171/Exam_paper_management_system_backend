package com.exam_paper.backend.dto.lecturer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private String notificationId;
    private String message;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
