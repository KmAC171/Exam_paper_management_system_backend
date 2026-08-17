package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.PrintingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintingScheduleRepository
        extends JpaRepository<PrintingSchedule, String> {

    List<PrintingSchedule> findByPacketPacketId(String packetId);
}