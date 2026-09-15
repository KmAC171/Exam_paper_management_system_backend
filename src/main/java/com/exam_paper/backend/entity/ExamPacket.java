package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "exam_packets", uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_cycle", columnNames = {"course_id", "cycle_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPacket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private AcademicCycle academicCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private User lecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private PacketStatus status;

    private LocalDate deadline;

    private String duration;
    private Integer totalMarks;
    private String questions;
    private String format;
    private String moderatorNote;
    private LocalDate moderationDeadline;
    private LocalDate examDate;

    @Column(name = "number_of_copies")
    private Integer numberOfCopies;

    @OneToOne(mappedBy = "packet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Marking marking;
}