package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacketDTO {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private String moderatorName;
    private LocalDate deadline;
    private boolean overdue;
    private String status;
    private String priority;
}
