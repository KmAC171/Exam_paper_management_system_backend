package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AssignedPacketDTO {

    private Long packetId;
    private String courseCode;
    private String courseName;
    private String departmentName;
    private String status;
    private LocalDate deadline;
}