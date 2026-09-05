package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponseDTO {
    private Long departmentId;
    private String departmentName;
    private Long hodUserId;
    private String hodFullName;
    private String hodUsername;
    private long totalCourses;
    private long totalLecturers;
    private long activePacketsCount;
    private List<UserDTO> staff;
}
