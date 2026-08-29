package com.exam_paper.backend.controller;

import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.CourseRepository;
import com.exam_paper.backend.repository.PacketStatusRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/form-data")
@RequiredArgsConstructor
public class FormDataController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PacketStatusRepository packetStatusRepository;

    @GetMapping
    public Map<String, Object> getFormData() {
        List<Course> courses = courseRepository.findAllByOrderByCourseName();
        List<User> lecturers = userRepository.findByRole(User.Role.ROLE_USER);
        List<User> moderators = userRepository.findByRole(User.Role.ROLE_MODERATOR);
        List<PacketStatus> statuses = packetStatusRepository.findAll();

        return Map.of(
                "courses", courses.stream().map(c -> Map.of(
                        "id", c.getCourseId(),
                        "code", c.getCourseCode(),
                        "name", c.getCourseName()
                )).toList(),
                "lecturers", lecturers.stream().map(u -> Map.of(
                        "id", u.getUserId(),
                        "name", u.getFullName()
                )).toList(),
                "moderators", moderators.stream().map(u -> Map.of(
                        "id", u.getUserId(),
                        "name", u.getFullName()
                )).toList(),
                "statuses", statuses.stream().map(s -> Map.of(
                        "id", s.getStatusId(),
                        "name", s.getStatusName()
                )).toList()
        );
    }
}