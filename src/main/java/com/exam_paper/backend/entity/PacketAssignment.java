package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;



@Entity
@Table(name = "packet_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketAssignment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;



    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;



    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



    private String assignedRole;


    private LocalDate assignedDate;



}