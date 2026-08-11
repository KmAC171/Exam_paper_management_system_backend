package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name="packet_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketAssignment {


    @Id
    @Column(name="assignment_id", length=10)
    private String assignmentId;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name="packet_id",
            referencedColumnName="packet_id"
    )
    private ExamPacket packet;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name="user_id",
            referencedColumnName="user_id"
    )
    private User user;


    @Column(name="assigned_role")
    private String assignedRole;


    @Column(name="assigned_date")
    private LocalDate assignedDate;

}