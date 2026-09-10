package com.exam_paper.backend.dto.printing;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintingScheduleResponseDTO {
    private Long scheduleId;
    private Long packetId;
    private String courseCode;
    private String courseName;
    private String departmentName;
    private Long lecturerId;
    private String lecturerName;
    private String lecturerEmail;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeLabel;
    private String location;
    private String status;
    private Integer copies;
    private String notes;
    private LocalDate examDate;
    private String packetStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
