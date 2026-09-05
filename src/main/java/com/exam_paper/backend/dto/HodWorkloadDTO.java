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
public class HodWorkloadDTO {
    private Long lecturerId;
    private String lecturerName;
    private String username;
    private String email;
    private String role;

    private List<String> assignedCourses;
    private long totalAssignedPackets;
    private long pendingPackets;
    private long draftPackets;
    private long submittedPackets;
    private long approvedPackets;
    private long printingPackets;
    private long completedPackets;
    private long rejectedPackets;
    private long overduePackets;

    private int totalScripts;
    private int markedScripts;
    private int progressPercentage;
}
