package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class NotificationResponseDTO {


    private String notificationId;

    private String message;

    private String type;

    private String status;

    private LocalDateTime createdAt;

}