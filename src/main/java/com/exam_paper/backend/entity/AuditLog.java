package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {


    @Id
    @Column(length=10)
    private String logId;



    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;



    private String action;


    private String entity;



    private LocalDateTime timestamp;

}