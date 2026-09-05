package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HodDashboardDTO {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String faculty;

    private long totalPackets;
    private long pendingPackets;
    private long draftPackets;
    private long submittedPackets;
    private long approvedPackets;
    private long printingPackets;
    private long completedPackets;
    private long rejectedPackets;
    private long overduePackets;

    private long totalCourses;
    private long totalStaff;

    private List<PacketDTO> recentPackets;
    private List<ActivityLogDTO> recentActivities;
}
