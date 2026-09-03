package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkingResponseDTO {
    private String packetId;
    private Integer totalScripts;
}
