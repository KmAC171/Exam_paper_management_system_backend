package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;
    private final CourseRepository courseRepository;
    private final PacketRepository packetRepository;
    private final PacketCommentRepository packetCommentRepository;
    private final PacketAttachmentRepository packetAttachmentRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Map<String, String> ROLE_LABELS = Map.of(
            "ROLE_ADMIN", "Asst. Registrar",
            "ROLE_GUEST", "Head of Dept.",
            "ROLE_USER", "Lecturer",
            "ROLE_MODERATOR", "Moderator"
    );

    private static final List<String> AVATAR_COLORS = List.of(
            "bg-blue-500", "bg-green-500", "bg-purple-500",
            "bg-yellow-500", "bg-red-500", "bg-pink-500",
            "bg-indigo-500", "bg-teal-500"
    );

    public UserManagementResponseDTO getUserManagement() {
        List<User> users = userRepository.findAllWithDepartment();

        UserStatsDTO stats = new UserStatsDTO(
                userRepository.count(),
                userRepository.countByRole(User.Role.ROLE_USER),
                userRepository.countByRole(User.Role.ROLE_MODERATOR),
                userRepository.countByIsActiveTrue()
        );

        List<UserManagementDTO> userDTOs = users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new UserManagementResponseDTO(stats, userDTOs);
    }

    @Transactional
    public void deleteUser(Long userId) {
        // 1. Delete notifications sent directly to this user
        notificationRepository.deleteByUser_UserId(userId);

        // 2. Disassociate courses where user is assigned as lecturer or moderator
        List<Course> lecCourses = courseRepository.findByLecturer_UserId(userId);
        if (!lecCourses.isEmpty()) {
            for (Course c : lecCourses) {
                c.setLecturer(null);
            }
            courseRepository.saveAll(lecCourses);
        }

        List<Course> modCourses = courseRepository.findByModerator_UserId(userId);
        if (!modCourses.isEmpty()) {
            for (Course c : modCourses) {
                c.setModerator(null);
            }
            courseRepository.saveAll(modCourses);
        }

        // 3. Disassociate packets where user is assigned as lecturer or moderator
        List<ExamPacket> lecPackets = packetRepository.findByLecturer_UserId(userId);
        if (!lecPackets.isEmpty()) {
            for (ExamPacket p : lecPackets) {
                p.setLecturer(null);
            }
            packetRepository.saveAll(lecPackets);
        }

        List<ExamPacket> modPackets = packetRepository.findByModerator_UserId(userId);
        if (!modPackets.isEmpty()) {
            for (ExamPacket p : modPackets) {
                p.setModerator(null);
            }
            packetRepository.saveAll(modPackets);
        }

        // 4. Disassociate comments authored by this user
        List<PacketComment> comments = packetCommentRepository.findByUser_UserId(userId);
        if (!comments.isEmpty()) {
            for (PacketComment c : comments) {
                c.setUser(null);
            }
            packetCommentRepository.saveAll(comments);
        }

        // 5. Disassociate attachments uploaded by this user
        List<PacketAttachment> attachments = packetAttachmentRepository.findByUploadedBy_UserId(userId);
        if (!attachments.isEmpty()) {
            for (PacketAttachment a : attachments) {
                a.setUploadedBy(null);
            }
            packetAttachmentRepository.saveAll(attachments);
        }

        // 6. Delete the user
        userRepository.deleteById(userId);
    }

    public void toggleActive(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(!user.isActive());
            userRepository.save(user);
        });
    }

    private UserManagementDTO toDTO(User u) {
        String initials = "??";
        if (u.getFullName() != null && !u.getFullName().trim().isEmpty()) {
            String[] parts = u.getFullName().trim().split("\\s+");
            if (parts.length > 1 && !parts[1].isEmpty()) {
                initials = parts[0].substring(0, 1) + parts[1].substring(0, 1);
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2);
            } else {
                initials = parts[0].substring(0, 1);
            }
        }

        int colorIndex = Math.abs(u.getFullName() != null
                ? u.getFullName().hashCode() % AVATAR_COLORS.size() : 0);
        String avatarColor = AVATAR_COLORS.get(Math.abs(colorIndex));

        String roleName = u.getRole() != null ? u.getRole().name() : "";
        String roleLabel = u.getRole() != null ? ROLE_LABELS.getOrDefault(roleName, roleName) : "";

        return new UserManagementDTO(
                u.getUserId(),
                u.getFullName(),
                u.getUsername(),
                u.getEmail() != null ? u.getEmail() : "",
                roleName,
                roleLabel,
                u.getDepartment() != null ? u.getDepartment().getDepartmentName() : "—",
                u.isActive(),
                formatLastLogin(u.getLastLogin()),
                initials.toUpperCase(),
                avatarColor
        );
    }

    private String formatLastLogin(LocalDateTime dt) {
        if (dt == null) return "Never";
        long minutes = ChronoUnit.MINUTES.between(dt, LocalDateTime.now());
        if (minutes < 60) return "Today, " + dt.format(DateTimeFormatter.ofPattern("h:mm a"));
        long hours = ChronoUnit.HOURS.between(dt, LocalDateTime.now());
        if (hours < 24) return "Today, " + dt.format(DateTimeFormatter.ofPattern("h:mm a"));
        long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
        if (days == 1) return "Yesterday";
        return dt.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    public UserManagementDTO createUser(UserDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        String username = dto.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + username + "' already exists. Please choose a different username.");
        }

        String email = dto.getEmail() != null ? dto.getEmail().trim() : null;
        if (email != null && !email.isEmpty()) {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Email address '" + email + "' is already registered to another user.");
            }
        }

        User.Role userRole;
        try {
            userRole = User.Role.valueOf(dto.getRole());
        } catch (Exception e) {
            userRole = User.Role.ROLE_USER;
        }

        Department department = dto.getDepartmentId() != null
                ? departmentRepository.findById(dto.getDepartmentId()).orElse(null)
                : null;

        User user = User.builder()
                .fullName(dto.getFullName() != null ? dto.getFullName().trim() : "")
                .username(username)
                .email(email != null && !email.isEmpty() ? email : null)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(userRole)
                .department(department)
                .isActive(true)
                .lastLogin(null)
                .build();

        return toDTO(userRepository.save(user));
    }

    public UserManagementDTO updateUser(Long userId, UpdateUserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String email = dto.getEmail() != null ? dto.getEmail().trim() : null;
        if (email != null && !email.isEmpty()) {
            if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(email, userId)) {
                throw new IllegalArgumentException("Email address '" + email + "' is already in use by another user.");
            }
            user.setEmail(email);
        } else {
            user.setEmail(null);
        }

        user.setFullName(dto.getFullName() != null ? dto.getFullName().trim() : "");
        user.setActive(dto.isActive());

        User.Role userRole;
        try {
            userRole = User.Role.valueOf(dto.getRole());
        } catch (Exception e) {
            userRole = User.Role.ROLE_USER;
        }
        user.setRole(userRole);

        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId())
                    .ifPresent(user::setDepartment);
        } else {
            user.setDepartment(null);
        }

        // only update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return toDTO(userRepository.save(user));
    }
}