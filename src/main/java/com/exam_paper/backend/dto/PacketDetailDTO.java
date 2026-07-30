package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacketDetailDTO {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String department;
    private String lecturerName;
    private String moderatorName;
    private LocalDate deadline;
    private LocalDate moderationDeadline;
    private LocalDate examDate;
    private String status;
    private String priority;
    private boolean overdue;

    // exam details
    private String duration;
    private Integer totalMarks;
    private String questions;
    private String format;
    private String moderatorNote;
}