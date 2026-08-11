package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CourseRepository
        extends JpaRepository<Course,String>{


    List<Course> findByCourseCodeContaining(String code);


    List<Course> findByCourseNameContaining(String name);


}