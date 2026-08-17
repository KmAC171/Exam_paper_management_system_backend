package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LecturerPrintingScheduleDTO {

    private String packetId;

    private String courseCode;

    private String courseName;

    private String status;

    private LocalDate deadline;
}