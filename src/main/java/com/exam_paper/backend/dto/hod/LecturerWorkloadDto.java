package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerWorkloadDto {
    private String lecturerId;
    private String lecturerName;
    private long totalAssignedPackets;
    private long totalScripts;
    private long markedScripts;
    private double progressPercentage;
}