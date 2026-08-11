package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name="markings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marking {


    @Id
    @Column(length=10)
    private String markingId;



    @OneToOne
    @JoinColumn(name="packet_id")
    private ExamPacket packet;



    @ManyToOne
    @JoinColumn(name="lecturer_id")
    private User lecturer;



    private Integer totalScripts;


    private Integer markedScripts;


    private LocalDate deadline;

}