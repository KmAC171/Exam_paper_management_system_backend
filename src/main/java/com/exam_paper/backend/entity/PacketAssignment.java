package com.exam_paper.backend.entity;

import com.exam_paper.backend.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "packet_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketAssignment {

    @Id
    @Column(name = "assignment_id", length = 50)
    private String assignmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "packet_id",
            referencedColumnName = "packetId"
    )
    private ExamPacket packet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "userId"
    )
    private User user;

    @Column(name = "assigned_role")
    private String assignedRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;
}
