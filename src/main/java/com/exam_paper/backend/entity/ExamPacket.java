package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;



@Entity
@Table(name = "exam_packets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPacket {


    @Id
    @Column(name = "packet_id", length = 10)
    private String packetId;


    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;


    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private AcademicCycle academicCycle;


    @Column(name = "status")
    private String status;


    @Column(name = "deadline")
    private LocalDate deadline;


    @ManyToOne
    @JoinColumn(name = "current_holder_id")
    private User currentHolder;


    @OneToMany(mappedBy = "packet")
    private List<PacketAssignment> assignments;


    @OneToMany(mappedBy = "packet")
    private List<PacketMovement> movements;


    @OneToMany(mappedBy = "packet")
    private List<Comment> comments;


    @OneToOne(mappedBy = "packet")
    private Marking marking;

}