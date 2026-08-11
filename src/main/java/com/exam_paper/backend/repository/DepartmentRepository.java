package com.exam_paper.backend.repository;


import com.example.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DepartmentRepository
        extends JpaRepository<Department,String> {


}