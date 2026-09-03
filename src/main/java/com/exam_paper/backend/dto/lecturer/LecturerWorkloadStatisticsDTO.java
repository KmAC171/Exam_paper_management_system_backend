package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
