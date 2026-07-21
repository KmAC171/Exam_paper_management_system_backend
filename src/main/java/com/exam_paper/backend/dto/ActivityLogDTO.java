package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private String message;
    private String actorInitials;
    private String actorColor;
    private String timeAgo;
}