package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PacketMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacketMovementRepository extends JpaRepository<PacketMovement, String> {
    List<PacketMovement> findByPacketPacketIdOrderByTimestampAsc(Long packetId);
    List<PacketMovement> findByPacketPacketId(Long packetId);
}
