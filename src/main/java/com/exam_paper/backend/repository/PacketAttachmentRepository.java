package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PacketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacketAttachmentRepository extends JpaRepository<PacketAttachment, Long> {
    List<PacketAttachment> findByPacket_PacketIdOrderByUploadedAtDesc(Long packetId);
}