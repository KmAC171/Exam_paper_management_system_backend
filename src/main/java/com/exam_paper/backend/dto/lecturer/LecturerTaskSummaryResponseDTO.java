package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LecturerTaskSummaryResponseDTO {

    private String lecturerId;

    private long pendingTasks;

    private long completedTasks;

    private long overdueTasks;

}