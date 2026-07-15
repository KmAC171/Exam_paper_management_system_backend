package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.time.LocalDate;


@Entity
@Table(name = "exam_packets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamPacket {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packetId;


    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;


    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private AcademicCycle academicCycle;


    private String status;


    private LocalDate deadline;



    @ManyToOne
    @JoinColumn(name = "current_holder_id")
    private User currentHolder;

    @OneToMany(mappedBy = "packet")
    private List<PacketAssignment> assignments;

}