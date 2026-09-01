package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeratorRecentReviewDTO {
    private String courseCode;
    private String note;
    private String status;
    private String date;
    private String iconType; // RETURN, APPROVE, PENDING, REJECT
}
