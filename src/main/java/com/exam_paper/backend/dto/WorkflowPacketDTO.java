package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowPacketDTO {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String status;
    private int currentStage;     // 1-6
    private int totalStages;      // always 6
    private List<WorkflowStageDTO> stages;
}