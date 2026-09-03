package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PrintingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrintingScheduleRepository extends JpaRepository<PrintingSchedule, String> {
    List<PrintingSchedule> findByPacketPacketId(Long packetId);
}
