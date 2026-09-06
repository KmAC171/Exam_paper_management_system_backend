package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        List<ExamPacket> allPackets = examPacketRepository.findAll();
        long total = allPackets.size();

        long pending = 0;
        long draft = 0;
        long underModeration = 0;
        long approved = 0;
        long printing = 0;
        long papersStored = 0;
        long answerSheetsTaken = 0;
        long marking = 0;
        long completed = 0;
        long delayed = 0;

        for (ExamPacket p : allPackets) {
            String rawStatus = p.getStatus() != null ? p.getStatus().getStatusName() : "PENDING";
            int stageIdx = WorkflowService.getStageIndexForStatus(rawStatus);

            boolean isDone = (stageIdx == 8);

            if (p.getDeadline() != null && p.getDeadline().isBefore(today) && !isDone) {
                delayed++;
            }

            switch (stageIdx) {
                case 0 -> pending++;
                case 1 -> draft++;
                case 2 -> underModeration++;
                case 3 -> approved++;
                case 4 -> printing++;
                case 5 -> papersStored++;
                case 6 -> answerSheetsTaken++;
                case 7 -> marking++;
                case 8 -> completed++;
                default -> pending++;
            }
        }

        List<StatusBreakdownItemDTO> breakdown = List.of(
                new StatusBreakdownItemDTO("PENDING", "Pending Assignment", pending, calcPercentage(pending, total), "#94a3b8", "bg-slate-50 text-slate-700", 1),
                new StatusBreakdownItemDTO("DRAFT", "Drafting", draft, calcPercentage(draft, total), "#f59e0b", "bg-amber-50 text-amber-700", 2),
                new StatusBreakdownItemDTO("UNDER_MODERATION", "Under Moderation", underModeration, calcPercentage(underModeration, total), "#3b82f6", "bg-blue-50 text-blue-700", 3),
                new StatusBreakdownItemDTO("APPROVED", "Approved", approved, calcPercentage(approved, total), "#10b981", "bg-emerald-50 text-emerald-700", 4),
                new StatusBreakdownItemDTO("PRINTING", "Printing Queue", printing, calcPercentage(printing, total), "#8b5cf6", "bg-purple-50 text-purple-700", 5),
                new StatusBreakdownItemDTO("PAPERS_STORED", "Papers Stored", papersStored, calcPercentage(papersStored, total), "#6366f1", "bg-indigo-50 text-indigo-700", 6),
                new StatusBreakdownItemDTO("ANSWER_SHEETS_TAKEN", "Answer Sheets Taken", answerSheetsTaken, calcPercentage(answerSheetsTaken, total), "#ec4899", "bg-pink-50 text-pink-700", 7),
                new StatusBreakdownItemDTO("MARKING", "In Marking", marking, calcPercentage(marking, total), "#f97316", "bg-orange-50 text-orange-700", 8),
                new StatusBreakdownItemDTO("COMPLETED", "Completed", completed, calcPercentage(completed, total), "#14b8a6", "bg-teal-50 text-teal-700", 9),
                new StatusBreakdownItemDTO("DELAYED", "Delayed", delayed, calcPercentage(delayed, total), "#ef4444", "bg-red-50 text-red-700", 10)
        );

        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalPackets(total)
                .pending(pending)
                .draft(draft)
                .underModeration(underModeration)
                .approved(approved)
                .printingQueue(printing)
                .papersStored(papersStored)
                .answerSheetsTaken(answerSheetsTaken)
                .marking(marking)
                .completed(completed)
                .delayed(delayed)
                .breakdown(breakdown)
                .build();

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

    private double calcPercentage(long count, long total) {
        if (total <= 0 || count <= 0) return 0.0;
        return Math.round(((double) count / total) * 1000.0) / 10.0;
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