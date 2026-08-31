package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PacketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PacketStatusRepository extends JpaRepository<PacketStatus, Long> {
    List<PacketStatus> findAll();
    Optional<PacketStatus> findByStatusName(String statusName);
}