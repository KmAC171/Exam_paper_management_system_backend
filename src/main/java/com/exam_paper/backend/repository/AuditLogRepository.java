package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AuditLogRepository
        extends JpaRepository<AuditLog,Long>{


    Optional<AuditLog>
    findTopByPacketPacketIdOrderByTimestampDesc(
            Long packetId
    );

}