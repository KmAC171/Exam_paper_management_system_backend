package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalPackets;
    private long pending;
    private long draft;
    private long underModeration;
    private long approved;
    private long printingQueue;
    private long papersStored;
    private long answerSheetsTaken;
    private long marking;
    private long completed;
    private long delayed;
    private List<StatusBreakdownItemDTO> breakdown;

    public DashboardSummaryDTO(long totalPackets, long pending, long approved, long delayed, long printingQueue, long underModeration) {
        this.totalPackets = totalPackets;
        this.pending = pending;
        this.approved = approved;
        this.delayed = delayed;
        this.printingQueue = printingQueue;
        this.underModeration = underModeration;
        this.draft = 0;
        this.papersStored = 0;
        this.answerSheetsTaken = 0;
        this.marking = 0;
        this.completed = 0;
    }
}
