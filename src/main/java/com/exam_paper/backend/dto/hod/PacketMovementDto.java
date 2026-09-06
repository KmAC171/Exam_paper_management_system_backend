package com.exam_paper.backend.dto.hod;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketMovementDto {
    private String movementId;
    private String fromUserName;
    private String toUserName;
    private String action;
    private LocalDateTime timestamp;
}
