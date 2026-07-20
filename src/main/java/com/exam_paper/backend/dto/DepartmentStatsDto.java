package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDto {
    private String departmentName;
    private long submitted;  // total packets in that department
    private long approved;
    private long delayed;
}