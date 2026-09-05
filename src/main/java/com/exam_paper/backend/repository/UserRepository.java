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
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndDepartment_DepartmentId(User.Role role, Long departmentId);
    Optional<User> findFirstByRoleAndDepartment_DepartmentId(User.Role role, Long departmentId);

    List<User> findByDepartment_DepartmentId(Long departmentId);

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, Long userId);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Long userId);

    long countByRole(User.Role role);
    long countByIsActiveTrue();
    long countByDepartment_DepartmentId(Long departmentId);
    long countByRoleAndDepartment_DepartmentId(User.Role role, Long departmentId);
}