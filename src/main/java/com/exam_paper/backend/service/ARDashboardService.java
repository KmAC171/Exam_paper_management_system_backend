package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.DashboardSummaryDTO;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ARDashboardService {
    private final ExamPacketRepository examPacketRepository;

    public DashboardSummaryDTO getSummary() {
        long total = examPacketRepository.count();
        long pending = examPacketRepository.countByStatus_StatusName("PENDING");
        long approved = examPacketRepository.countByStatus_StatusName("APPROVED");
        long printingQueue = examPacketRepository.countByStatus_StatusName("PRINTING_QUEUE");
        long delayed = examPacketRepository.countDelayed(LocalDate.now());

        return new DashboardSummaryDTO(total, pending, approved, delayed, printingQueue);
    }
}
