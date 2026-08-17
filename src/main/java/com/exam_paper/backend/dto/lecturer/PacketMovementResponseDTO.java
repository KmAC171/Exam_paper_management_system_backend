package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketMovementResponseDTO {

    private String movementId;

    private String fromUser; // Holds the sender's name/identifier

    private String toUser;   // Holds the receiver's name/identifier

    private String action;

    private LocalDateTime timestamp;
}