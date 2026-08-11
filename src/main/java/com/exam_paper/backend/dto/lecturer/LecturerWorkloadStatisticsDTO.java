package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LecturerWorkloadStatisticsDTO {

    private String lecturerId;

    private long totalAssignedPackets;

    private int totalScripts;

    private int markedScripts;

    private int remainingScripts;

    private long completedPackets;

    private long pendingPackets;

    private long overduePackets;

}