package com.exam_paper.backend.dto.lecturer;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerDeadlineCalendarDTO {
    private String packetId;
    private String courseCode;
    private String courseName;
    private LocalDate deadline;
    private String status;
}
