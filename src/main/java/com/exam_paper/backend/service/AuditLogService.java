package com.exam_paper.backend.service;

import com.exam_paper.backend.entity.AuditLog;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.AuditLogRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // =========================================================
    // LOG ACTION
    // =========================================================
    public void logAction(
            Long userId,
            String action,
            String entity,
            Long entityId,
            String details
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    // =========================================================
    // USER HISTORY
    // =========================================================
    public List<AuditLog> getUserActivityHistory(Long userId) {
        return auditLogRepository.findByUserUserIdOrderByTimestampDesc(userId);
    }

    // =========================================================
    // ENTITY HISTORY
    // =========================================================
    public List<AuditLog> getEntityHistory(String entity, Long entityId) {
        return auditLogRepository.findByEntityAndEntityId(entity, entityId);
    }

    // =========================================================
    // DATE RANGE
    // =========================================================
    public List<AuditLog> getActivityBetweenDates(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }
}