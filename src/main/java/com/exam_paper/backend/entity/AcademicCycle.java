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
    @Column(length = 20)
    private String cycleId;

    private Integer year;

    private Integer semester;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;
}
