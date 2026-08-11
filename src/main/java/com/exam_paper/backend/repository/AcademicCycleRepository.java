package com.exam_paper.backend.repository;


import com.example.backend.entity.AcademicCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AcademicCycleRepository
        extends JpaRepository<AcademicCycle,String>{




    Optional<AcademicCycle> findByStatus(String status);


}