package com.exam_paper.backend.controller;

import com.exam_paper.backend.entity.AuditLog;
import com.exam_paper.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    // =========================================================
    // GET USER ACTIVITY HISTORY
    // GET /api/audit-logs/user/{userId}
    // =========================================================
    @GetMapping("/user/{userId}")
    public List<AuditLog> getUserActivityHistory(
            @PathVariable Long userId
    ) {
        return service.getUserActivityHistory(userId);
    }

    // =========================================================
    // GET ENTITY HISTORY
    // GET /api/audit-logs/entity?entity=EXAM_PACKET&entityId=1
    // =========================================================
    @GetMapping("/entity")
    public List<AuditLog> getEntityHistory(
            @RequestParam String entity,
            @RequestParam Long entityId
    ) {
        return service.getEntityHistory(entity, entityId);
    }

    // =========================================================
    // GET ACTIVITY BETWEEN DATES
    // GET /api/audit-logs/activity?start=2026-01-01T00:00:00&end=2026-12-31T23:59:59
    // =========================================================
    @GetMapping("/activity")
    public List<AuditLog> getActivityBetweenDates(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return service.getActivityBetweenDates(start, end);
    }
}
