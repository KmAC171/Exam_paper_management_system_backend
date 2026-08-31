package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.UserManagementResponseDTO;
import com.exam_paper.backend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<UserManagementResponseDTO> getUsers() {
        return ResponseEntity.ok(userManagementService.getUserManagement());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        userManagementService.toggleActive(id);
        return ResponseEntity.ok().build();
    }
}
