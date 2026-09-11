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
    private final com.exam_paper.backend.repository.PacketRepository packetRepository;

    @GetMapping
    public Map<String, Object> getFormData() {
        List<Course> courses = courseRepository.findAllWithDepartmentOrderByCourseCodeAsc();
        List<User> lecturers = userRepository.findByRole(User.Role.ROLE_USER);
        List<User> moderatorCandidates = new java.util.ArrayList<>(lecturers);
        for (User u : userRepository.findByRole(User.Role.ROLE_MODERATOR)) {
            if (!moderatorCandidates.contains(u)) {
                moderatorCandidates.add(u);
            }
        }
        List<PacketStatus> statuses = packetStatusRepository.findAll();

        return Map.of(
                "courses", courses.stream().map(c -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", c.getCourseId());
                    map.put("code", c.getCourseCode());
                    map.put("name", c.getCourseName());
                    map.put("department", c.getDepartment() != null ? c.getDepartment().getDepartmentName() : "Unassigned");
                    map.put("lecturerId", c.getLecturer() != null ? c.getLecturer().getUserId() : null);
                    map.put("lecturerName", c.getLecturer() != null ? c.getLecturer().getFullName() : "Unassigned");
                    map.put("hasPacket", packetRepository.existsByCourse_CourseId(c.getCourseId()));
                    return map;
                }).toList(),
                "lecturers", lecturers.stream().map(u -> Map.of(
                        "id", u.getUserId(),
                        "name", u.getFullName()
                )).toList(),
                "moderators", moderatorCandidates.stream().map(u -> Map.of(
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