package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.DashboardResponseDTO;
import com.exam_paper.backend.dto.DashboardSummaryDTO;
import com.exam_paper.backend.dto.DepartmentStatsDto;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExamPacketRepository examPacketRepository;

    public DashboardResponseDTO getDashboard() {
        LocalDate today = LocalDate.now();

        long total = examPacketRepository.count();
        long pending = examPacketRepository.countByStatus_StatusName("PENDING");
        long approved = examPacketRepository.countByStatus_StatusName("APPROVED");
        long printingQueue = examPacketRepository.countByStatus_StatusName("PRINTING_QUEUE");
        long underModeration = examPacketRepository.countByStatus_StatusName("UNDER_MODERATION");
        long delayed = examPacketRepository.countDelayed(today);

        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                total, pending, approved, delayed, printingQueue, underModeration
        );

        List<DepartmentStatsDto> departmentStats = examPacketRepository
                .getDepartmentStats(today)
                .stream()
                .map(p -> new DepartmentStatsDto(
                        p.getDepartmentName(),
                        p.getSubmitted(),
                        p.getApproved(),
                        p.getDelayed()
                ))
                .collect(Collectors.toList());

        return new DashboardResponseDTO(summary, departmentStats); // ✅ return
    }
}