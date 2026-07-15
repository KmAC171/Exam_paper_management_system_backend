package com.exam_paper.backend.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketMovementResponseDTO {


    private String fromUser;

    private String toUser;

    private String action;

    private LocalDateTime timestamp;

}