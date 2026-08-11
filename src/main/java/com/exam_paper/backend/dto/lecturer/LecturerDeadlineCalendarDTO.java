package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LecturerDeadlineCalendarDTO {

    private String packetId;

    private String courseCode;

    private String courseName;

    private LocalDate deadline;

    private String status;

}