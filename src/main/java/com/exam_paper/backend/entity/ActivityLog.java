package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String actorInitials;
    private String actorColor;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String actorName;
    private String stageName;

    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;
}
