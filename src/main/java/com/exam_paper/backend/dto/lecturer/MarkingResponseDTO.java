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
    private Integer markedScripts;
    private Integer remainingScripts;
    private Double progress;

    public MarkingResponseDTO(String packetId, Integer totalScripts) {
        this.packetId = packetId;
        this.totalScripts = totalScripts;
        this.markedScripts = 0;
        this.remainingScripts = totalScripts != null ? totalScripts : 0;
        this.progress = 0.0;
    }
}
