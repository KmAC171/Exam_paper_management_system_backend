package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowPacketDTO {
    private Long id;
    private String packetId;
    private String courseCode;
    private String courseName;
    private String status;
    private int currentStage;     // 1-10
    private int totalStages;      // 10
    private String lecturerUsername;
    private String lecturerName;
    private String moderatorUsername;
    private String moderatorName;
    private List<WorkflowStageDTO> stages;
}