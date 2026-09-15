package com.exam_paper.backend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiCycleTrendDTO {
    private List<CycleSummaryDTO> cycleSummaries;
    private List<DepartmentTrendDTO> departmentTrends;
    private List<SubmissionPatternDTO> submissionPatterns;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CycleSummaryDTO {
        private String cycleId;
        private String cycleName;
        private String academicYear;
        private Integer semester;
        private long totalPackets;
        private long completedPackets;
        private long delayedPackets;
        private double onTimeRate;
        private double completionRate;
        private double avgProcessingDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentTrendDTO {
        private String departmentName;
        private String cycleId;
        private String cycleName;
        private long totalPackets;
        private long onTime;
        private long delayed;
        private double onTimeRate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionPatternDTO {
        private String cycleId;
        private String cycleName;
        private String periodLabel; // e.g. "Week 1", "Week 2", "Month 1", etc.
        private long submittedCount;
        private long approvedCount;
    }
}
