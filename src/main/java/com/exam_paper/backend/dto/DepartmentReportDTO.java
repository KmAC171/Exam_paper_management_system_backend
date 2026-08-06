package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportDTO {
    private String department;
    private long totalPackets;
    private long onTime;
    private long delayed;
    private double onTimeRate;
}