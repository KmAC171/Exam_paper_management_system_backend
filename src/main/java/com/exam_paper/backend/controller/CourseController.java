package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.CoursePageResponseDTO;
import com.exam_paper.backend.dto.CourseRequestDTO;
import com.exam_paper.backend.dto.CourseResponseDTO;
import com.exam_paper.backend.repository.DepartmentRepository;
import com.exam_paper.backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<CoursePageResponseDTO> getCourses(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(courseService.getCourses(username, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourseById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(courseService.getCourseById(id, username, role));
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseRequestDTO dto, Authentication authentication) {
        try {
            String username = authentication.getName();
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            CourseResponseDTO created = courseService.createCourse(dto, username, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to create course."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id,
                                          @RequestBody CourseRequestDTO dto,
                                          Authentication authentication) {
        try {
            String username = authentication.getName();
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            CourseResponseDTO updated = courseService.updateCourse(id, dto, username, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to update course."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            courseService.deleteCourse(id, username, role);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to delete course."));
        }
    }

    @GetMapping("/departments")
    public List<Map<String, Object>> getDepartments() {
        return departmentRepository.findAllByOrderByDepartmentName()
                .stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getDepartmentId(),
                        "name", d.getDepartmentName()))
                .toList();
    }

    @GetMapping("/staff-options")
    public ResponseEntity<?> getStaffOptions() {
        return ResponseEntity.ok(courseService.getStaffOptions());
    }
}
