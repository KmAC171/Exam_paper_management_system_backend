package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.Department;
import com.exam_paper.backend.repository.DepartmentRepository;
import com.exam_paper.backend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final DepartmentRepository departmentRepository;

    @GetMapping
    public UserManagementResponseDTO getUsers() {
        return userManagementService.getUserManagement();
    }

    @PostMapping
    public UserManagementDTO createUser(@RequestBody UserDTO dto) {
        return userManagementService.createUser(dto);
    }

    @PutMapping("/{id}")
    public UserManagementDTO updateUser(@PathVariable Long id,
                                        @RequestBody UpdateUserDTO dto) {
        return userManagementService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
    }

    @PutMapping("/{id}/toggle-active")
    public void toggleActive(@PathVariable Long id) {
        userManagementService.toggleActive(id);
    }

    @GetMapping("/departments")
    public List<Map<String, Object>> getDepartments() {
        return departmentRepository.findAllByOrderByDepartmentName()
                .stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getDepartmentId(),
                        "name", d.getDepartmentName()))
                .toList();
    }
}