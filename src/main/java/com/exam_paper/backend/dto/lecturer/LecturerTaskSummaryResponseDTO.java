package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerTaskSummaryResponseDTO {
    private String lecturerId;
    private long pendingTasks;
    private long completedTasks;
    private long overdueTasks;
}
