package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @Column(name = "comment_id")
    private String commentId;

    // =========================================================
    // PACKET
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "packet_id",
            nullable = false
    )
    private ExamPacket packet;

    // =========================================================
    // USER
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    // =========================================================
    // COMMENT TEXT
    // =========================================================

    @Column(
            name = "comment_text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String commentText;

    // =========================================================
    // TIMESTAMP
    // =========================================================

    @Column(
            name = "timestamp",
            nullable = false
    )
    private LocalDateTime timestamp;
}