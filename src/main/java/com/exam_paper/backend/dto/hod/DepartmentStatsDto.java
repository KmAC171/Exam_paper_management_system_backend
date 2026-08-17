package com.exam_paper.backend.dto.hod;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentStatsDto {
    private long totalPackets;
    private long inProgressPackets;
    private long overduePackets;
    private long completedPackets;
}