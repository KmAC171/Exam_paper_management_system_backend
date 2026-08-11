package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LecturerMarkingProcessDTO {

    private String markingId;

    private String packetId;

    private String courseCode;

    private String courseName;

    private int totalScripts;

    private int markedScripts;

    private int remainingScripts;

    private double progress;

    private LocalDate deadline;

}