package com.exam_paper.backend.repository;


import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository
        extends JpaRepository<User,String>{


    Optional<User> findByEmail(String email);


    List<User> findByDepartmentDeptId(String deptId);


    List<User> findByRoleRoleName(String roleName);


}