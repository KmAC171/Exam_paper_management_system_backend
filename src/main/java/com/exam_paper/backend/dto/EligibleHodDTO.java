package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibleHodDTO {
    private Long userId;
    private String username;
    private String fullName;
    private Long currentDepartmentId;
    private String currentDepartmentName;
}
