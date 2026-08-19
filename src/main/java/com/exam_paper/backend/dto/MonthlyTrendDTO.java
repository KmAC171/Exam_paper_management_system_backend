package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDTO {
    private String month;
    private long submitted;
    private long approved;
    private long delayed;
}