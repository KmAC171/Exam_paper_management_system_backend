package com.exam_paper.backend.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String fullName;
    private String role;
    private String email;
    private Long departmentId;
}
