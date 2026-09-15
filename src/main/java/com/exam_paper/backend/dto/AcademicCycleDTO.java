package com.exam_paper.backend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicCycleDTO {
    private String cycleId;
    private String academicYear;
    private Integer year;
    private Integer semester;
    private String cycleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, PLANNED, COMPLETED, ARCHIVED
    private long totalPackets;
    private long completedPackets;
    private long delayedPackets;
    private double completionRate;
}
