package com.exam_paper.backend.dto.hod;

import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
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
