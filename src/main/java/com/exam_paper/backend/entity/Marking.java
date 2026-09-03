package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "markings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marking {

    @Id
    @Column(name = "marking_id", length = 50, nullable = false)
    private String markingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "packet_id",
            nullable = false
    )
    private ExamPacket packet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "lecturer_id",
            nullable = false
    )
    private User lecturer;

    @Column(name = "total_scripts")
    private Integer totalScripts;

    @Column(name = "marked_scripts")
    private Integer markedScripts;

    @Column(name = "deadline")
    private LocalDate deadline;
}
