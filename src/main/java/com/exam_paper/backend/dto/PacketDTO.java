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
public class PacketDTO {
    private Long id;
    private String packetId;
    private String courseCode;
    private String courseName;
    private Long lecturerId;
    private String lecturerName;
    private String lecturerUsername;
    private Long moderatorId;
    private String moderatorName;
    private String moderatorUsername;
    private LocalDate deadline;
    private boolean overdue;
    private String status;
    private String priority;

    // Backward-compatible constructor for existing tests
    public PacketDTO(Long id, String packetId, String courseCode, String courseName,
                     String lecturerName, String moderatorName, LocalDate deadline,
                     boolean overdue, String status, String priority) {
        this.id = id;
        this.packetId = packetId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturerName = lecturerName;
        this.moderatorName = moderatorName;
        this.deadline = deadline;
        this.overdue = overdue;
        this.status = status;
        this.priority = priority;
    }
}
