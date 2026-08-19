package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.ActivityLogDTO;
import com.exam_paper.backend.entity.ActivityLog;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void log(String message, String actorInitials, String actorColor) {
        ActivityLog log = ActivityLog.builder()
                .message(message)
                .actorInitials(actorInitials)
                .actorColor(actorColor)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    public void logForPacket(ExamPacket packet, String stageName, String message, String actorName, String actorInitials, String actorColor ) {
        ActivityLog log = ActivityLog.builder()
                .packet(packet)
                .stageName(stageName)
                .message(message)
                .actorName(actorName)
                .actorInitials(actorInitials)
                .actorColor(actorColor)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getRecentActivity() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<ActivityLog> getPacketHistory(Long packetId) {
        return activityLogRepository
                .findByPacket_PacketIdOrderByCreatedAtAsc(packetId);
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

    public List<ActivityLogDTO> getRecentActivityDTOs() {
        return getRecentActivity().stream()
                .map(log -> new ActivityLogDTO(
                        log.getMessage(),
                        log.getActorInitials(),
                        log.getActorColor(),
                        timeAgo(log.getCreatedAt())
                ))
                .collect(Collectors.toList());
    }
}