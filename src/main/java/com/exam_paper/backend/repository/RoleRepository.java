package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository
        extends JpaRepository<Role,
        String> {


}