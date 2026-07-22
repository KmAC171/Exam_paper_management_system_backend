package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "markings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Marking {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long markingId;


    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;


    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private User lecturer;


    private Integer scriptCount;


    private Integer completedScripts = 0;


    private LocalDateTime createdAt;



    @PrePersist
    public void onCreate(){

        createdAt = LocalDateTime.now();

    }

}