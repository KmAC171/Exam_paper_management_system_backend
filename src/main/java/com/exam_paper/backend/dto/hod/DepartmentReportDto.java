package com.exam_paper.backend.dto.hod;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
