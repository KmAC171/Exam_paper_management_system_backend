package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPacketResponseDto {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String cycleId;
    private String status;
    private LocalDate deadline;
    private String currentHolderId;
    private String currentHolderName;
    private boolean isOverdue;
}