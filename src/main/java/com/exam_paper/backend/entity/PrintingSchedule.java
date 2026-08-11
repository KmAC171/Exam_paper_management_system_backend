package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRINTING_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintingSchedule {

    @Id
    @Column(name = "schedule_id", length = 10)
    private String scheduleId;

    @ManyToOne
    @JoinColumn(name = "packet_id", referencedColumnName = "packet_id")
    private ExamPacket packet;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "status", length = 30)
    private String status; // e.g., 'Scheduled', 'In Progress', 'Completed'
}