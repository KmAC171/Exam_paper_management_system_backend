package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportDto {
    private String departmentId;
    private String departmentName;
    private long totalPackets;
    private long pendingPackets;
    private long inProgressPackets;
    private long completedPackets;
    private long overduePackets;
    private List<LecturerWorkloadDto> workloadDistribution;
}