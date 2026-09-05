package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDTO {
    private String courseCode;
    private String courseName;
    private Long departmentId;
    private Long lecturerId;
    private Long moderatorId;
}
