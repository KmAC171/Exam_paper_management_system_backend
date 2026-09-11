package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketDetailDTO {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String department;
    private Long lecturerId;
    private String lecturerName;
    private String lecturerUsername;
    private Long moderatorId;
    private String moderatorName;
    private String moderatorUsername;
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