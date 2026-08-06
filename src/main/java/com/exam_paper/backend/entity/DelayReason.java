package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delay_reasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelayReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;

    private String reason;
}