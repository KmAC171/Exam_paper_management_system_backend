package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // User activity history
    List<AuditLog> findByUserUserIdOrderByTimestampDesc(Long userId);

    // Entity + entityId history
    List<AuditLog> findByEntityAndEntityId(String entity, Long entityId);

    // Date range history
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start,
            LocalDateTime end
    );
}