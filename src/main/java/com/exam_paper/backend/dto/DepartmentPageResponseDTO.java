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
public class DepartmentPageResponseDTO {
    private DepartmentSummaryStatsDTO stats;
    private List<DepartmentResponseDTO> departments;
}
