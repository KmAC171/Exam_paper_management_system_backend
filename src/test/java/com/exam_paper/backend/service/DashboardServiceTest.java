package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.DashboardResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.repository.DepartmentStatsProjection;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.SubmissionTrendProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private ExamPacketRepository examPacketRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private PacketService packetService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    public void testGetDashboard_HandlesNullDeadlinesAndProjectionsSafely() {
        ExamPacket packetWithoutDeadline = ExamPacket.builder()
                .packetId(1L)
                .status(new PacketStatus(1L, "PENDING"))
                .deadline(null)
                .build();

        when(examPacketRepository.findByAcademicCycle_CycleId(any())).thenReturn(List.of(packetWithoutDeadline));

        SubmissionTrendProjection nullMonthProjection = new SubmissionTrendProjection() {
            @Override
            public Integer getMonth() {
                return null;
            }

            @Override
            public Long getCount() {
                return 5L;
            }
        };

        when(examPacketRepository.getSubmissionTrendByCycle(any()))
                .thenReturn(List.of(nullMonthProjection));

        DepartmentStatsProjection deptStats = new DepartmentStatsProjection() {
            @Override
            public String getDepartmentName() {
                return "Computer Science";
            }

            @Override
            public Long getSubmitted() {
                return null;
            }

            @Override
            public Long getApproved() {
                return null;
            }

            @Override
            public Long getDelayed() {
                return null;
            }
        };

        when(examPacketRepository.getDepartmentStatsByCycle(any(), any()))
                .thenReturn(List.of(deptStats));

        when(activityLogService.getRecentActivity()).thenReturn(Collections.emptyList());

        DashboardResponseDTO result = dashboardService.getDashboard("2025-2026-SEM1");

        assertNotNull(result);
        assertNotNull(result.getSummary());
        assertEquals(1, result.getSummary().getTotalPackets());
        assertEquals(1, result.getDepartmentStats().size());
        assertEquals(0L, result.getDepartmentStats().get(0).getSubmitted());
        assertEquals(0, result.getSubmissionTrend().size());
    }
}
