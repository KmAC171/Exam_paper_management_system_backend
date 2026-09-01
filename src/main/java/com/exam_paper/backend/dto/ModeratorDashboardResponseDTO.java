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
public class ModeratorDashboardResponseDTO {
    private ModeratorKpiDTO kpis;
    private List<ModeratorPendingPacketDTO> pendingPackets;
    private List<ModeratorRecentReviewDTO> recentReviews;
}
