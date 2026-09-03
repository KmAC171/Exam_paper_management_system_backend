package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "printing_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintingSchedule {

    @Id
    @Column(name = "schedule_id", length = 50)
    private String scheduleId;

    @ManyToOne
    @JoinColumn(name = "packet_id", referencedColumnName = "packetId")
    private ExamPacket packet;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "status", length = 30)
    private String status;
}
