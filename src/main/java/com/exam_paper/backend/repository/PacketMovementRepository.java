package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PacketMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacketMovementRepository extends JpaRepository<PacketMovement, String> {

    // Fetch movements ordered chronologically (oldest to newest) for history trails
    List<PacketMovement> findByPacketPacketIdOrderByTimestampAsc(String packetId);

    // Alternative unordered find if needed
    List<PacketMovement> findByPacketPacketId(String packetId);
}