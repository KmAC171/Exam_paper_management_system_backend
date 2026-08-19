package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department ORDER BY u.fullName")
    List<User> findAllWithDepartment();

    long countByRole(User.Role role);
    long countByIsActiveTrue();
}