package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStageDTO {
    private String stageName;
    private String actor;
    private boolean completed;
    private boolean current;
    private List<WorkflowEventDTO> events;
}