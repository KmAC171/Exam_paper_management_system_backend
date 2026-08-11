package com.exam_paper.backend.dto.lecturer;


import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketMovementResponseDTO {


    private String movementId;


    private String fromUser;


    private String toUser;


    private String action;


    private LocalDateTime timestamp;

}