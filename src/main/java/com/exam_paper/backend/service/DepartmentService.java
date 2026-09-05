package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.Department;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.CourseRepository;
import com.exam_paper.backend.repository.DepartmentRepository;
import com.exam_paper.backend.repository.PacketRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PacketRepository packetRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public DepartmentPageResponseDTO getDepartments(String username, String role) {
        List<DepartmentResponseDTO> departmentDTOs = new ArrayList<>();
        String userDeptName = null;

        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() != null) {
                Department dept = hod.getDepartment();
                userDeptName = dept.getDepartmentName();
                departmentDTOs.add(buildDepartmentDTO(dept));
            }
        } else {
            List<Department> departments = departmentRepository.findAllByOrderByDepartmentName();
            for (Department dept : departments) {
                departmentDTOs.add(buildDepartmentDTO(dept));
            }
        }

        long totalDepartments = departmentRepository.count();
        long totalHODsAssigned = userRepository.findByRole(User.Role.ROLE_GUEST).stream()
                .filter(u -> u.getDepartment() != null)
                .count();
        long totalFacultyCourses = courseRepository.count();
        long totalFacultyStaff = userRepository.count();

        DepartmentSummaryStatsDTO stats = DepartmentSummaryStatsDTO.builder()
                .totalDepartments(totalDepartments)
                .totalHODsAssigned(totalHODsAssigned)
                .totalFacultyCourses(totalFacultyCourses)
                .totalFacultyStaff(totalFacultyStaff)
                .userDepartmentName(userDeptName)
                .build();

        return DepartmentPageResponseDTO.builder()
                .stats(stats)
                .departments(departmentDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id, String username, String role) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() == null || !hod.getDepartment().getDepartmentId().equals(id)) {
                throw new RuntimeException("Access denied: You can only view your own department.");
            }
        }

        return buildDepartmentDTO(dept);
    }

    @Transactional
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto, String username) {
        if (dto.getDepartmentName() == null || dto.getDepartmentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required.");
        }

        String name = dto.getDepartmentName().trim();
        if (departmentRepository.existsByDepartmentNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Department name '" + name + "' already exists.");
        }

        Department department = new Department();
        department.setDepartmentName(name);
        Department saved = departmentRepository.save(department);

        User assignedHod = null;
        if (dto.getHodUserId() != null) {
            assignedHod = userRepository.findById(dto.getHodUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getHodUserId()));
            assignedHod.setDepartment(saved);
            userRepository.save(assignedHod);
        }

        // Notify ARs
        notificationService.notifyAR(
                "New Department Created",
                "Department '" + saved.getDepartmentName() + "' has been created by " + username + ".",
                "SYSTEM",
                false,
                username
        );

        // Notify assigned HOD
        if (assignedHod != null) {
            notificationService.createNotification(
                    assignedHod,
                    null,
                    "HOD Assignment",
                    "You have been assigned as Head of Department for '" + saved.getDepartmentName() + "'.",
                    "SYSTEM",
                    true
            );
        }

        return buildDepartmentDTO(saved);
    }

    @Transactional
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO dto, String username) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        if (dto.getDepartmentName() == null || dto.getDepartmentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required.");
        }

        String name = dto.getDepartmentName().trim();
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(name, id)) {
            throw new IllegalArgumentException("Department name '" + name + "' is already in use.");
        }

        dept.setDepartmentName(name);
        Department saved = departmentRepository.save(dept);

        User previousHod = userRepository.findFirstByRoleAndDepartment_DepartmentId(User.Role.ROLE_GUEST, id).orElse(null);

        if (dto.getHodUserId() != null) {
            if (previousHod == null || !previousHod.getUserId().equals(dto.getHodUserId())) {
                if (previousHod != null) {
                    previousHod.setDepartment(null);
                    userRepository.save(previousHod);
                }

                User newHod = userRepository.findById(dto.getHodUserId())
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getHodUserId()));
                newHod.setDepartment(saved);
                userRepository.save(newHod);

                notificationService.createNotification(
                        newHod,
                        null,
                        "HOD Assignment",
                        "You have been assigned as Head of Department for '" + saved.getDepartmentName() + "'.",
                        "SYSTEM",
                        true
                );
            }
        } else {
            if (previousHod != null) {
                previousHod.setDepartment(null);
                userRepository.save(previousHod);
            }
        }

        // Notify AR and HOD
        notificationService.notifyARAndHOD(
                saved,
                "Department Details Updated",
                "Department '" + saved.getDepartmentName() + "' details were updated by " + username + ".",
                "SYSTEM",
                false,
                username
        );

        return buildDepartmentDTO(saved);
    }

    @Transactional
    public void deleteDepartment(Long id, String username) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // 1. Unassign all users from this department
        List<User> users = userRepository.findByDepartment_DepartmentId(id);
        for (User u : users) {
            u.setDepartment(null);
        }
        userRepository.saveAll(users);

        // 2. Unassign all courses from this department
        List<Course> courses = courseRepository.findByDepartment_DepartmentIdOrderByCourseCodeAsc(id);
        for (Course c : courses) {
            c.setDepartment(null);
        }
        courseRepository.saveAll(courses);

        // 3. Delete the department
        departmentRepository.delete(dept);

        // 4. Notify ARs
        notificationService.notifyAR(
                "Department Deleted",
                "Department '" + dept.getDepartmentName() + "' was deleted by " + username + ". Any associated staff and courses have been set to unassigned.",
                "SYSTEM",
                false,
                username
        );
    }

    @Transactional(readOnly = true)
    public List<EligibleHodDTO> getEligibleHods() {
        return userRepository.findByRole(User.Role.ROLE_GUEST).stream()
                .map(u -> EligibleHodDTO.builder()
                        .userId(u.getUserId())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .currentDepartmentId(u.getDepartment() != null ? u.getDepartment().getDepartmentId() : null)
                        .currentDepartmentName(u.getDepartment() != null ? u.getDepartment().getDepartmentName() : "Unassigned")
                        .build())
                .collect(Collectors.toList());
    }

    private DepartmentResponseDTO buildDepartmentDTO(Department dept) {
        User hod = userRepository.findFirstByRoleAndDepartment_DepartmentId(User.Role.ROLE_GUEST, dept.getDepartmentId()).orElse(null);
        long totalCourses = courseRepository.countByDepartment_DepartmentId(dept.getDepartmentId());
        long totalLecturers = userRepository.countByRoleAndDepartment_DepartmentId(User.Role.ROLE_USER, dept.getDepartmentId());
        long activePackets = packetRepository.countByCourse_Department_DepartmentId(dept.getDepartmentId());

        List<UserDTO> staffList = userRepository.findByDepartment_DepartmentId(dept.getDepartmentId()).stream()
                .map(u -> UserDTO.builder()
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .departmentId(dept.getDepartmentId())
                        .build())
                .collect(Collectors.toList());

        return DepartmentResponseDTO.builder()
                .departmentId(dept.getDepartmentId())
                .departmentName(dept.getDepartmentName())
                .hodUserId(hod != null ? hod.getUserId() : null)
                .hodFullName(hod != null ? hod.getFullName() : null)
                .hodUsername(hod != null ? hod.getUsername() : null)
                .totalCourses(totalCourses)
                .totalLecturers(totalLecturers)
                .activePacketsCount(activePackets)
                .staff(staffList)
                .build();
    }
}
