package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "academic_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicCycle {

    @Id
    @Column(name = "cycle_id", length = 50)
    private String cycleId;

    @Column(name = "academic_year", length = 30)
    private String academicYear; // e.g. "2025/2026"

    @Column(name = "year_number")
    private Integer year; // e.g. 2025

    @Column(name = "semester")
    private Integer semester; // 1 or 2

    @Column(name = "cycle_name", length = 100)
    private String cycleName; // e.g. "2025/2026 - Semester 1"

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 30)
    private String status; // "ACTIVE", "PLANNED", "COMPLETED", "ARCHIVED"
}

