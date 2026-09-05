package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long departmentId;
    private String departmentName;
    private Long lecturerId;
    private String lecturerName;
    private Long moderatorId;
    private String moderatorName;
    private long activePacketsCount;
}
