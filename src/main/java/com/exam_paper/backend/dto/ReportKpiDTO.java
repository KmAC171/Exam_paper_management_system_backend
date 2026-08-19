package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportKpiDTO {
    private double completionRate;
    private double avgProcessingDays;
    private double onTimeSubmissionRate;
    private long packetsInDelay;
}