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
public class CourseStaffOptionsDTO {
    private List<StaffOptionDTO> lecturers;
    private List<StaffOptionDTO> moderators;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StaffOptionDTO {
        private Long id;
        private String name;
        private String username;
        private String email;
        private Long departmentId;
        private String departmentName;
    }
}
