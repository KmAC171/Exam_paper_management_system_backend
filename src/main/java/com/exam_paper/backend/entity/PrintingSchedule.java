package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "PRINTING_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrintingSchedule {

    @Id
    @Column(name = "schedule_id")
    private String scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;

    @Column(name = "printing_date")
    private LocalDate printingDate;

    @Column(name = "printing_time")
    private LocalTime printingTime;

    @Column(name = "printing_location")
    private String printingLocation;

    @Column(name = "status")
    private String status;

    @Column(name = "remarks")
    private String remarks;

}