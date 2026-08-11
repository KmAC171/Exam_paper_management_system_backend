package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketMovementDto {
    private String movementId;
    private String fromUserName;
    private String toUserName;
    private String action;
    private LocalDateTime timestamp;
}