package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPacketResponseDto {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String cycleId;
    private String academicCycle;
    private String stage;
    private String status;
    private LocalDate deadline;
    private String currentHolderId;
    private String currentHolder;
    private String currentHolderName;
    private String lecturerId;
    private String lecturerName;
    private String moderatorName;
    private Long totalPapers;
    private Long papersToMark;
    private LocalDateTime lastUpdatedTime;
    private String lastUpdatedUser;
    private boolean isOverdue;
}