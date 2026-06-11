package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamPacketRepository extends JpaRepository<ExamPacket, Long> {

    List<ExamPacket> findByLecturerUserId(Long lecturerId);
}