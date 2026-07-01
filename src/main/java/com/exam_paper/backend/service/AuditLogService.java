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

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDetails(details);

        auditLogRepository.save(log);
    }

    // =========================================================
    // GET USER ACTIVITY HISTORY
    // =========================================================
    public List<AuditLog> getUserActivityHistory(Long userId) {

        return auditLogRepository.findByUserUserIdOrderByTimestampDesc(userId);
    }

    // =========================================================
    // GET ENTITY HISTORY
    // =========================================================
    public List<AuditLog> getEntityHistory(String entity, Long entityId) {

        return auditLogRepository.findByEntityAndEntityId(entity, entityId);
    }

    // =========================================================
    // GET ACTIVITY BETWEEN DATES
    // =========================================================
    public List<AuditLog> getActivityBetweenDates(
            LocalDateTime start,
            LocalDateTime end
    ) {

        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }
}
