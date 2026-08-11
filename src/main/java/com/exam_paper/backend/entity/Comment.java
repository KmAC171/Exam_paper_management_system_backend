package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {


    @Id
    @Column(length=50)
    private String commentId;



    @ManyToOne
    @JoinColumn(name="packet_id")
    private ExamPacket packet;



    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;



    @Column(columnDefinition="TEXT")
    private String commentText;



    private LocalDateTime timestamp;

}