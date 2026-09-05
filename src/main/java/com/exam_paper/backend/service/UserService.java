package com.exam_paper.backend.service;

import com.exam_paper.backend.Security.JwtUtill;
import com.exam_paper.backend.dto.UserDTO;
import com.exam_paper.backend.entity.User;
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
            throw new IllegalArgumentException("Username is required.");
        }
        String username = dto.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken. Please choose another.");
        }

        String email = dto.getEmail() != null ? dto.getEmail().trim() : null;
        if (email != null && !email.isEmpty() && userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email address '" + email + "' is already registered.");
        }

        User.Role userRole;
        try {
            userRole = User.Role.valueOf(dto.getRole());
        } catch (Exception e) {
            userRole = User.Role.ROLE_USER;
        }

        User user = User.builder()
                .username(username)
                .fullName(dto.getFullName() != null ? dto.getFullName().trim() : "")
                .email(email != null && !email.isEmpty() ? email : null)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(userRole)
                .build();
        userRepository.save(user);
    }

    public void logout(String token) {
        Date expiry = jwtUtill.extractExpiration(token);
        tokenBlacklistService.blacklistToken(token, expiry);
    }

}
