package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOrderByCourseName();
    List<Course> findAllByOrderByCourseCodeAsc();

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department LEFT JOIN FETCH c.lecturer LEFT JOIN FETCH c.moderator ORDER BY c.courseCode ASC")
    List<Course> findAllWithDepartmentOrderByCourseCodeAsc();

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department LEFT JOIN FETCH c.lecturer LEFT JOIN FETCH c.moderator WHERE c.department.departmentId = :departmentId ORDER BY c.courseCode ASC")
    List<Course> findByDepartment_DepartmentIdOrderByCourseCodeAsc(@Param("departmentId") Long departmentId);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department LEFT JOIN FETCH c.lecturer LEFT JOIN FETCH c.moderator WHERE c.courseId = :id")
    Optional<Course> findByIdWithDepartment(@Param("id") Long id);

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCaseAndCourseIdNot(String courseCode, Long courseId);

    boolean existsByCourseNameIgnoreCaseAndDepartment_DepartmentId(String courseName, Long departmentId);

    boolean existsByCourseNameIgnoreCaseAndDepartment_DepartmentIdAndCourseIdNot(String courseName, Long departmentId, Long courseId);

    long countByDepartment_DepartmentId(Long departmentId);

    boolean existsByDepartment_DepartmentId(Long departmentId);

    List<Course> findByLecturer_UserId(Long userId);

    List<Course> findByModerator_UserId(Long userId);
}