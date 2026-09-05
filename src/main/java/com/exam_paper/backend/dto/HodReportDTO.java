package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HodReportDTO {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String faculty;

    private long totalPackets;
    private long completedPackets;
    private long inProgressPackets;
    private long overduePackets;
    private int completionPercentage;

    private List<CourseBreakdownDTO> courseBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseBreakdownDTO {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String semester;
        private String academicYear;
        private String lecturerName;
        private String moderatorName;
        private String status;
        private String deadline;
        private boolean overdue;
    }
}
