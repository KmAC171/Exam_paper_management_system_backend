package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.Department;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.CourseRepository;
import com.exam_paper.backend.repository.DepartmentRepository;
import com.exam_paper.backend.repository.PacketRepository;
import com.exam_paper.backend.repository.PacketStatusRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PacketRepository packetRepository;
    private final PacketStatusRepository packetStatusRepository;
    private final ActivityLogService activityLogService;
    private final PacketService packetService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public CoursePageResponseDTO getCourses(String username, String role) {
        List<Course> courses;
        String userDeptName = null;

        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() == null) {
                courses = List.of();
            } else {
                userDeptName = hod.getDepartment().getDepartmentName();
                courses = courseRepository.findByDepartment_DepartmentIdOrderByCourseCodeAsc(hod.getDepartment().getDepartmentId());
            }
        } else {
            courses = courseRepository.findAllWithDepartmentOrderByCourseCodeAsc();
        }

        List<CourseResponseDTO> dtos = courses.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        long totalCourses = dtos.size();
        long totalDepartments = courses.stream()
                .map(c -> c.getDepartment() != null ? c.getDepartment().getDepartmentId() : null)
                .filter(id -> id != null)
                .distinct()
                .count();

        long totalPackets = dtos.stream()
                .mapToLong(CourseResponseDTO::getActivePacketsCount)
                .sum();

        CourseStatsDTO stats = CourseStatsDTO.builder()
                .totalCourses(totalCourses)
                .totalDepartments(totalDepartments)
                .totalPacketsLinked(totalPackets)
                .userDepartmentName(userDeptName)
                .build();

        return CoursePageResponseDTO.builder()
                .stats(stats)
                .courses(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(Long id, String username, String role) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() == null || course.getDepartment() == null ||
                    !hod.getDepartment().getDepartmentId().equals(course.getDepartment().getDepartmentId())) {
                throw new RuntimeException("Access denied: You can only view courses in your department.");
            }
        }

        return toDTO(course);
    }

    @Transactional
    public CourseResponseDTO createCourse(CourseRequestDTO dto, String username, String role) {
        if (dto.getCourseCode() == null || dto.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code is required");
        }
        if (dto.getCourseName() == null || dto.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }

        String code = dto.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Course code '" + code + "' already exists. Course codes must be unique.");
        }

        String name = dto.getCourseName().trim();

        Department department;
        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() == null) {
                throw new IllegalStateException("HOD does not have an assigned department.");
            }
            department = hod.getDepartment();
        } else {
            if (dto.getDepartmentId() == null) {
                throw new IllegalArgumentException("Department is required");
            }
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + dto.getDepartmentId()));
        }

        if (department != null && courseRepository.existsByCourseNameIgnoreCaseAndDepartment_DepartmentId(name, department.getDepartmentId())) {
            throw new IllegalArgumentException("A course with name '" + name + "' already exists in department '" + department.getDepartmentName() + "'.");
        }

        if (dto.getLecturerId() != null && dto.getModeratorId() != null && dto.getLecturerId().equals(dto.getModeratorId())) {
            throw new IllegalArgumentException("A Lecturer cannot be assigned as the Moderator for the same course.");
        }

        User lecturer = null;
        if (dto.getLecturerId() != null) {
            lecturer = userRepository.findById(dto.getLecturerId()).orElse(null);
        }

        User moderator = null;
        if (dto.getModeratorId() != null) {
            moderator = userRepository.findById(dto.getModeratorId()).orElse(null);
        }

        Course course = new Course();
        course.setCourseCode(code);
        course.setCourseName(dto.getCourseName().trim());
        course.setDepartment(department);
        course.setLecturer(lecturer);
        course.setModerator(moderator);

        Course saved = courseRepository.save(course);

        // Auto-create initial ExamPacket for this course so it immediately appears on the packets page
        PacketStatus initialStatus = packetStatusRepository.findByStatusName("PENDING")
                .or(() -> packetStatusRepository.findByStatusName("DRAFT"))
                .orElse(null);

        ExamPacket packet = new ExamPacket();
        packet.setCourse(saved);
        packet.setLecturer(saved.getLecturer());
        packet.setModerator(saved.getModerator());
        packet.setStatus(initialStatus);
        packet.setDuration("3 Hours");
        packet.setTotalMarks(100);
        packet.setQuestions("All sections mandatory");
        packet.setFormat("Standard Exam");
        ExamPacket savedPacket = packetRepository.save(packet);

        String creatorName = username != null ? username : "Academic Registry";
        activityLogService.logForPacket(
                savedPacket,
                initialStatus != null ? initialStatus.getStatusName() : "PENDING",
                "Exam packet initialized with PENDING status for course " + saved.getCourseCode() + " (" + saved.getCourseName() + ")",
                creatorName,
                "AR",
                "bg-blue-500"
        );

        // Notify assigned Lecturer
        if (saved.getLecturer() != null) {
            notificationService.createNotification(
                    saved.getLecturer(),
                    savedPacket,
                    "Course Assignment",
                    "You have been assigned as the Lecturer for course " + saved.getCourseCode() + " (" + saved.getCourseName() + ").",
                    "COURSE",
                    false
            );
        }

        // Notify assigned Moderator
        if (saved.getModerator() != null) {
            notificationService.createNotification(
                    saved.getModerator(),
                    savedPacket,
                    "Course Moderation Assignment",
                    "You have been assigned as the Moderator for course " + saved.getCourseCode() + " (" + saved.getCourseName() + ").",
                    "COURSE",
                    false
            );
        }

        // Trigger notifications to AR and HOD
        String staffSummary = "Lecturer: " + (saved.getLecturer() != null ? saved.getLecturer().getFullName() : "Unassigned") +
                ", Moderator: " + (saved.getModerator() != null ? saved.getModerator().getFullName() : "Unassigned");

        notificationService.notifyARAndHOD(
                saved.getDepartment(),
                "New Course Added",
                "Course " + saved.getCourseCode() + " (" + saved.getCourseName() + ") was added by " + username + " [" + staffSummary + "].",
                "COURSE",
                false,
                username
        );

        return toDTO(saved);
    }

    @Transactional
    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto, String username, String role) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if (dto.getCourseCode() == null || dto.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code is required");
        }
        if (dto.getCourseName() == null || dto.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }

        String code = dto.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCodeIgnoreCaseAndCourseIdNot(code, id)) {
            throw new IllegalArgumentException("Course code '" + code + "' is already in use by another course.");
        }

        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() == null || course.getDepartment() == null ||
                    !hod.getDepartment().getDepartmentId().equals(course.getDepartment().getDepartmentId())) {
                throw new RuntimeException("Access denied: You can only edit courses in your department.");
            }
        } else {
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found with id: " + dto.getDepartmentId()));
                course.setDepartment(department);
            }
        }

        Department dept = course.getDepartment();
        String name = dto.getCourseName().trim();
        if (dept != null && courseRepository.existsByCourseNameIgnoreCaseAndDepartment_DepartmentIdAndCourseIdNot(name, dept.getDepartmentId(), id)) {
            throw new IllegalArgumentException("A course with name '" + name + "' already exists in department '" + dept.getDepartmentName() + "'.");
        }

        course.setCourseCode(code);
        course.setCourseName(name);

        Long prevLecturerId = course.getLecturer() != null ? course.getLecturer().getUserId() : null;
        Long prevModeratorId = course.getModerator() != null ? course.getModerator().getUserId() : null;

        if (dto.getLecturerId() != null && dto.getModeratorId() != null && dto.getLecturerId().equals(dto.getModeratorId())) {
            throw new IllegalArgumentException("A Lecturer cannot be assigned as the Moderator for the same course.");
        }

        User newLecturer = null;
        if (dto.getLecturerId() != null) {
            newLecturer = userRepository.findById(dto.getLecturerId()).orElse(null);
        }
        course.setLecturer(newLecturer);

        User newModerator = null;
        if (dto.getModeratorId() != null) {
            newModerator = userRepository.findById(dto.getModeratorId()).orElse(null);
        }
        course.setModerator(newModerator);

        Course saved = courseRepository.save(course);

        // Sync packet staff assignments with updated course assignments
        List<ExamPacket> existingPackets = packetRepository.findByCourse_CourseId(saved.getCourseId());
        if (existingPackets.isEmpty()) {
            PacketStatus initialStatus = packetStatusRepository.findByStatusName("PENDING")
                    .or(() -> packetStatusRepository.findByStatusName("DRAFT"))
                    .orElse(null);

            ExamPacket newPacket = new ExamPacket();
            newPacket.setCourse(saved);
            newPacket.setLecturer(saved.getLecturer());
            newPacket.setModerator(saved.getModerator());
            newPacket.setStatus(initialStatus);
            newPacket.setDuration("3 Hours");
            newPacket.setTotalMarks(100);
            newPacket.setQuestions("All sections mandatory");
            newPacket.setFormat("Standard Exam");
            packetRepository.save(newPacket);
        } else {
            for (ExamPacket p : existingPackets) {
                p.setLecturer(saved.getLecturer());
                p.setModerator(saved.getModerator());
            }
            packetRepository.saveAll(existingPackets);
        }

        // Notify Lecturer if newly assigned or changed
        if (newLecturer != null && !newLecturer.getUserId().equals(prevLecturerId)) {
            notificationService.createNotification(
                    newLecturer,
                    null,
                    "Course Assignment",
                    "You have been assigned as the Lecturer for course " + saved.getCourseCode() + " (" + saved.getCourseName() + ").",
                    "COURSE",
                    false
            );
        }

        // Notify Moderator if newly assigned or changed
        if (newModerator != null && !newModerator.getUserId().equals(prevModeratorId)) {
            notificationService.createNotification(
                    newModerator,
                    null,
                    "Course Moderation Assignment",
                    "You have been assigned as the Moderator for course " + saved.getCourseCode() + " (" + saved.getCourseName() + ").",
                    "COURSE",
                    false
            );
        }

        // Trigger notifications to AR and HOD
        String staffSummary = "Lecturer: " + (saved.getLecturer() != null ? saved.getLecturer().getFullName() : "Unassigned") +
                ", Moderator: " + (saved.getModerator() != null ? saved.getModerator().getFullName() : "Unassigned");

        notificationService.notifyARAndHOD(
                saved.getDepartment(),
                "Course Updated",
                "Course " + saved.getCourseCode() + " (" + saved.getCourseName() + ") was updated by " + username + " [" + staffSummary + "].",
                "COURSE",
                false,
                username
        );

        return toDTO(saved);
    }

    @Transactional
    public void deleteCourse(Long id, String username, String role) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if ("ROLE_GUEST".equals(role)) {
            User hod = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            if (hod.getDepartment() == null || course.getDepartment() == null ||
                    !hod.getDepartment().getDepartmentId().equals(course.getDepartment().getDepartmentId())) {
                throw new RuntimeException("Access denied: You can only delete courses in your department.");
            }
        }

        // Delete all associated packets and clean up
        List<ExamPacket> packets = packetRepository.findByCourse_CourseId(id);
        for (ExamPacket p : packets) {
            packetService.deletePacket(p.getPacketId());
        }

        Department dept = course.getDepartment();
        String deletedCode = course.getCourseCode();
        String deletedName = course.getCourseName();

        courseRepository.delete(course);

        // Trigger notifications to AR and HOD
        notificationService.notifyARAndHOD(
                dept,
                "Course Deleted",
                "Course " + deletedCode + " (" + deletedName + ") was deleted by " + username + ".",
                "COURSE",
                false,
                username
        );
    }

    @Transactional(readOnly = true)
    public CourseStaffOptionsDTO getStaffOptions() {
        List<CourseStaffOptionsDTO.StaffOptionDTO> lecturers = userRepository.findByRole(User.Role.ROLE_USER).stream()
                .map(u -> CourseStaffOptionsDTO.StaffOptionDTO.builder()
                        .id(u.getUserId())
                        .name(u.getFullName())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .departmentId(u.getDepartment() != null ? u.getDepartment().getDepartmentId() : null)
                        .departmentName(u.getDepartment() != null ? u.getDepartment().getDepartmentName() : "Unassigned")
                        .build())
                .collect(Collectors.toList());

        List<User> modStaff = new java.util.ArrayList<>(userRepository.findByRole(User.Role.ROLE_USER));
        for (User u : userRepository.findByRole(User.Role.ROLE_MODERATOR)) {
            if (!modStaff.contains(u)) {
                modStaff.add(u);
            }
        }

        List<CourseStaffOptionsDTO.StaffOptionDTO> moderators = modStaff.stream()
                .map(u -> CourseStaffOptionsDTO.StaffOptionDTO.builder()
                        .id(u.getUserId())
                        .name(u.getFullName())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .departmentId(u.getDepartment() != null ? u.getDepartment().getDepartmentId() : null)
                        .departmentName(u.getDepartment() != null ? u.getDepartment().getDepartmentName() : "Unassigned")
                        .build())
                .collect(Collectors.toList());

        return CourseStaffOptionsDTO.builder()
                .lecturers(lecturers)
                .moderators(moderators)
                .build();
    }

    private CourseResponseDTO toDTO(Course course) {
        long count = packetRepository.countByCourse_CourseId(course.getCourseId());
        return CourseResponseDTO.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .departmentId(course.getDepartment() != null ? course.getDepartment().getDepartmentId() : null)
                .departmentName(course.getDepartment() != null ? course.getDepartment().getDepartmentName() : "Unassigned")
                .lecturerId(course.getLecturer() != null ? course.getLecturer().getUserId() : null)
                .lecturerName(course.getLecturer() != null ? course.getLecturer().getFullName() : null)
                .moderatorId(course.getModerator() != null ? course.getModerator().getUserId() : null)
                .moderatorName(course.getModerator() != null ? course.getModerator().getFullName() : null)
                .activePacketsCount(count)
                .build();
    }
}
