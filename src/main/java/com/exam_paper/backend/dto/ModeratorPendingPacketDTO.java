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
public class ModeratorPendingPacketDTO {
    private Long id;
    private String packetId;
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private String submittedDate;
    private String deadline;
    private LocalDate rawDeadline;
    private String priority;
    private String status;
    private String moderatorNote;
}
