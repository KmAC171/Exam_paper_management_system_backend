package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseStatsDTO {
    private long totalCourses;
    private long totalDepartments;
    private long totalPacketsLinked;
    private String userDepartmentName;
}
