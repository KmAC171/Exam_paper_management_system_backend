package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.AcademicCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicCycleRepository extends JpaRepository<AcademicCycle, String> {
    Optional<AcademicCycle> findByCycleId(String cycleId);
    List<AcademicCycle> findByStatus(String status);
}
