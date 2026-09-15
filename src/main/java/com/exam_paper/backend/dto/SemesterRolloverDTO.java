package com.exam_paper.backend.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

public class SemesterRolloverDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolloverRequest {
        private String sourceCycleId;
        private String targetAcademicYear; // e.g. "2026/2027"
        private Integer targetSemester;    // 1 or 2
        private String targetCycleName;    // e.g. "2026/2027 - Semester 1"
        private LocalDate startDate;
        private LocalDate endDate;
        @Builder.Default
        private boolean keepStaffAssignments = true;
        @Builder.Default
        private boolean autoShiftDeadlines = true;
        private List<Long> includedCourseIds; // optional selective list
        private List<CourseStaffOverride> staffOverrides; // optional lecturer/moderator overrides
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseStaffOverride {
        private Long courseId;
        private Long lecturerId;
        private Long moderatorId;
        private LocalDate customDeadline;
        private LocalDate customModerationDeadline;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolloverPreviewItem {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String departmentName;
        private Long lecturerId;
        private String lecturerName;
        private Long moderatorId;
        private String moderatorName;
        private LocalDate calculatedDeadline;
        private LocalDate calculatedModerationDeadline;
        private String defaultDuration;
        private Integer defaultTotalMarks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolloverPreviewResponse {
        private String sourceCycleId;
        private String sourceCycleName;
        private String targetCycleId;
        private String targetCycleName;
        private LocalDate targetStartDate;
        private LocalDate targetEndDate;
        private long totalCoursesToClone;
        private List<RolloverPreviewItem> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolloverExecutionResult {
        private String cycleId;
        private String cycleName;
        private String status;
        private int createdPacketsCount;
        private int assignedLecturersCount;
        private int assignedModeratorsCount;
        private String message;
    }
}
