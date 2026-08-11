package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name="academic_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicCycle {


    @Id
    @Column(length=10)
    private String cycleId;


    private Integer year;


    private Integer semester;


    private LocalDate startDate;


    private LocalDate endDate;


    private String status;



    @OneToMany(mappedBy="academicCycle")
    private List<ExamPacket> packets;

}