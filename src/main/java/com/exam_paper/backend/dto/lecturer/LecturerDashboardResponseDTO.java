package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LecturerDashboardResponseDTO {

    private String lecturerId;

    private long totalActiveTasks;

    private int scriptsToMark;

    private long completedTasks;

    private long overdueItems;

    private int totalScripts;

    private int markedScripts;

    private LocalDate nextDeadline;
}