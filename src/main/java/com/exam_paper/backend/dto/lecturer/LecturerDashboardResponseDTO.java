package com.exam_paper.backend.dto.lecturer;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerDashboardResponseDTO {
    private String lecturerId;
    private long totalActiveTasks;
    private long completedTasks;
    private long overdueItems;
    private int totalScripts;
    private LocalDate nextDeadline;
}
