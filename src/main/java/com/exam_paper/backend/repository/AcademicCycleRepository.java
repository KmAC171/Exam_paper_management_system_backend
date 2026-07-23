package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.AcademicCycle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface AcademicCycleRepository
        extends JpaRepository<AcademicCycle, Long> {



    List<AcademicCycle> findByStatus(
            String status
    );


}