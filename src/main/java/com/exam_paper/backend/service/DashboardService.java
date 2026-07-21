package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExamPacketRepository examPacketRepository;
    private final ActivityLogService activityLogService;

    private static final String[] MONTH_NAMES = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public DashboardResponseDTO getDashboard() {
        LocalDate today = LocalDate.now();

        // Summary
        long total = examPacketRepository.count();
        long pending = examPacketRepository.countByStatus_StatusName("PENDING");
        long approved = examPacketRepository.countByStatus_StatusName("APPROVED");
        long printingQueue = examPacketRepository.countByStatus_StatusName("PRINTING_QUEUE");
        long underModeration = examPacketRepository.countByStatus_StatusName("UNDER_MODERATION");
        long delayed = examPacketRepository.countDelayed(today);

        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                total, pending, approved, delayed, printingQueue, underModeration
        );

        // Department stats
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

        // Submission trend
        List<SubmissionTrendDTO> submissionTrend = examPacketRepository
                .getSubmissionTrend()
                .stream()
                .map(p -> new SubmissionTrendDTO(
                        MONTH_NAMES[p.getMonth()],
                        p.getCount()
                ))
                .collect(Collectors.toList());

        // Recent activity
        List<ActivityLogDTO> recentActivity = activityLogService.getRecentActivity()
                .stream()
                .map(log -> new ActivityLogDTO(
                        log.getMessage(),
                        log.getActorInitials(),
                        log.getActorColor(),
                        timeAgo(log.getCreatedAt())
                ))
                .collect(Collectors.toList());

        return new DashboardResponseDTO(
                summary, departmentStats, submissionTrend, recentActivity
        );
    }

    private String timeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + " hr ago";
        long days = ChronoUnit.DAYS.between(dateTime, now);
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }
}