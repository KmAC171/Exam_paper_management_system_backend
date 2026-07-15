package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LecturerDashboardDTO {

    private Long totalAssigned;
    private Long pending;
    private Long completed;
    private Long overdue;
}