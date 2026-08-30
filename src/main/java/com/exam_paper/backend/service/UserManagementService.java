package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void deleteUser(Long userId) {
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
}