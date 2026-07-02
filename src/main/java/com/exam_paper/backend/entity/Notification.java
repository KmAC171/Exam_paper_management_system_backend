package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;

    @Column(name = "message", length = 255, nullable = false)
    private String message;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "notification_type", length = 50)
    private String notificationType;

    @Column(name = "status", length = 20)
    private String status; // Unread / Read

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "Unread";
        }
    }
}