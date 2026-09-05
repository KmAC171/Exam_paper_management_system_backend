package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSummaryStatsDTO {
    private long totalDepartments;
    private long totalHODsAssigned;
    private long totalFacultyCourses;
    private long totalFacultyStaff;
    private String userDepartmentName;
}
