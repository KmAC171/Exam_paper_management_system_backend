package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PacketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacketCommentRepository extends JpaRepository<PacketComment, Long> {
    List<PacketComment> findByPacket_PacketIdOrderByCreatedAtAsc(Long packetId);
}