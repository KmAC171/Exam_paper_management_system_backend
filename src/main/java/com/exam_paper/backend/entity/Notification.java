package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {


    @Id
    @Column(length=10)
    private String notificationId;



    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;



    @Column(columnDefinition="TEXT")
    private String message;



    private String type;


    private String status;


    private LocalDateTime createdAt;

}