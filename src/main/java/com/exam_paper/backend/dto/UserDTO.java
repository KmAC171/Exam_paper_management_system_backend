package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String fullName;
    private String username;
    private String email;
    private String role;
    private String roleLabel;
    private String department;
    private boolean isActive;
    private String lastLogin;
    private String initials;
    private String avatarColor;
}