package com.exam_paper.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreatePacketDTO {
    private Long courseId;
    private Long lecturerId;
    private Long moderatorId;
    private Long statusId;
    private LocalDate deadline;
    private LocalDate moderationDeadline;
    private LocalDate examDate;
    private String duration;
    private Integer totalMarks;
    private String questions;
    private String format;
    private String moderatorNote;
}