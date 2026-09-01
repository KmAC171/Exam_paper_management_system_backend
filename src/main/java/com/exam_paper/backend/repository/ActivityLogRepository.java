package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();
    List<ActivityLog> findByPacket_PacketIdOrderByCreatedAtAsc(Long packetId);
    List<ActivityLog> findByPacket_Moderator_UserIdOrderByCreatedAtDesc(Long moderatorId);
}

