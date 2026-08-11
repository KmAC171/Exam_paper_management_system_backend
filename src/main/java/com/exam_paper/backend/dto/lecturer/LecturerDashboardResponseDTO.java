package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LecturerDashboardResponseDTO {

    private String lecturerId;

    private long totalAssignedPackets;

    private long pendingTasks;

    private long completedTasks;

    private long overdueTasks;

    private int totalScripts;

    private int markedScripts;

    private int remainingScripts;

    private LocalDate nextDeadline;

}