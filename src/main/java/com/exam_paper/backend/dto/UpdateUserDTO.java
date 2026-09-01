package com.exam_paper.backend.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String fullName;
    private String email;
    private String role;
    private Long departmentId;
    private boolean isActive;
    private String password; // optional — only update if not blank
}