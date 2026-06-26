package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExamPacketRepository extends JpaRepository<ExamPacket, Long> {

    List<ExamPacket> findByLecturerUserId(Long lecturerId);
    long countByStatus_StatusName(String statusName);

    @Query("SELECT COUNT(p) FROM ExamPacket p " + "WHERE p.status.statusName = 'PENDING'AND p.deadline < :today ")
    long countDelayed(@Param("today") LocalDate today);
}