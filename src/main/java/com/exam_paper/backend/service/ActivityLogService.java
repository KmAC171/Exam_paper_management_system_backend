package com.exam_paper.backend.service;

import com.exam_paper.backend.entity.ActivityLog;
import com.exam_paper.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<ActivityLog> getRecentActivity() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc();
    }
}