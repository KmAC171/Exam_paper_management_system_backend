package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteTaskResponseDTO {
    private String packetId;
    private String status;
    private String message;
}
