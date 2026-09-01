package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeratorKpiDTO {
    private long pendingReview;
    private long approvedToday;
    private long returned;
    private long totalReviewed;
}
