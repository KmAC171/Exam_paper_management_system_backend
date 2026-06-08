package com.exam_paper.backend.entity;

import jakarta.persistence.*;
        import lombok.*;
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
    @JoinColumn(name = "lecturer_id")
    private User lecturer;

    @ManyToOne
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private PacketStatus status;

    private LocalDate deadline;
}