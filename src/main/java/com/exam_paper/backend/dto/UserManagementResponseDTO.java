package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponseDTO {
    private UserStatsDTO stats;
    private List<UserManagementDTO> users;
}