package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class HistoryDTO {
    private String stageName;
    private String message;
    private String actorName;
    private String actorInitials;
    private String actorColor;
    private String createdAt;
}