package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByOrderByDepartmentName();
}