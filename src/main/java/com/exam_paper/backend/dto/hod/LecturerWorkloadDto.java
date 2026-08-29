package com.exam_paper.backend.dto.hod;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LecturerWorkloadDto {
    private String lecturerId;
    private String lecturerName;
    private long totalAssignedPackets;
    private long totalScripts;
    private long markedScripts;
    private double progressPercentage;
}
