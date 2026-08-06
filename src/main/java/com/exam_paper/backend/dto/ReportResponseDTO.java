package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private ReportKpiDTO kpi;
    private List<MonthlyTrendDTO> monthlyTrend;
    private List<DelayReasonDTO> delayReasons;
    private List<DepartmentReportDTO> departmentComparison;
}