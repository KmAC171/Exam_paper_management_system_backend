package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.DelayReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DelayReasonRepository extends JpaRepository<DelayReason, Long> {

    @Query("SELECT d.reason, COUNT(d) FROM DelayReason d GROUP BY d.reason ORDER BY COUNT(d) DESC")
    List<Object[]> countByReason();
}