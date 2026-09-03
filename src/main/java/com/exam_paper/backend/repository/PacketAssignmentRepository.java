package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PacketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacketAssignmentRepository extends JpaRepository<PacketAssignment, String> {
    List<PacketAssignment> findByUserUserId(Long userId);
    long countByUserUserId(Long userId);
    List<PacketAssignment> findByPacketPacketId(Long packetId);
}
