package com.exam_paper.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacketMovementDTO {

    private Long movementId;

    private Long packetId;

    private String fromUserName;

    private String toUserName;

    private String action;

    private LocalDateTime timestamp;

    private String remarks;
}