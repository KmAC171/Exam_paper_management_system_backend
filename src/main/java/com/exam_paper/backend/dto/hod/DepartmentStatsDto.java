package com.exam_paper.backend.dto.hod;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentStatsDto {
    private long totalPackets;
    private long completedPackets;
    private long overduePackets;
    private long inProgressPackets;
}
