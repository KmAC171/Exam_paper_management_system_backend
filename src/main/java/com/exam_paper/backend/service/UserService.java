package com.exam_paper.backend.service;

import com.exam_paper.backend.Security.JwtUtill;
import com.exam_paper.backend.dto.UserDTO;
import com.exam_paper.backend.entity.Department;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.DepartmentRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtill;
    private final TokenBlacklistService tokenBlacklistService;

    public String login(String username, String password){
        Optional<User> userOpt = userRepository.findByUsername(username);

        if(userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtill.generateToken(username, user.getRole().name());
            }
        }
        return null;
    }

    public void register(UserDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.findByUsername(dto.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Username '" + dto.getUsername().trim() + "' is already taken");
        }

        User.Role userRole;
        try {
            userRole = User.Role.valueOf(dto.getRole());
        } catch (Exception e) {
            userRole = User.Role.ROLE_USER;
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        }

        User user = User.builder()
                .username(dto.getUsername().trim())
                .fullName(dto.getFullName() != null && !dto.getFullName().trim().isEmpty() ? dto.getFullName().trim() : dto.getUsername().trim())
                .email(dto.getEmail() != null && !dto.getEmail().trim().isEmpty() ? dto.getEmail().trim() : null)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(userRole)
                .department(department)
                .isActive(true)
                .build();
        userRepository.save(user);
    }

    public void logout(String token) {
        Date expiry = jwtUtill.extractExpiration(token);
        tokenBlacklistService.blacklistToken(token, expiry);
    }

}

