package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<DepartmentPageResponseDTO> getDepartments(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(departmentService.getDepartments(username, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(departmentService.getDepartmentById(id, username, role));
    }

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentRequestDTO dto, Authentication authentication) {
        try {
            String username = authentication.getName();
            DepartmentResponseDTO created = departmentService.createDepartment(dto, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to create department."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id,
                                              @RequestBody DepartmentRequestDTO dto,
                                              Authentication authentication) {
        try {
            String username = authentication.getName();
            DepartmentResponseDTO updated = departmentService.updateDepartment(id, dto, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to update department."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            departmentService.deleteDepartment(id, username);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to delete department."));
        }
    }

    @GetMapping("/eligible-hods")
    public ResponseEntity<List<EligibleHodDTO>> getEligibleHods() {
        return ResponseEntity.ok(departmentService.getEligibleHods());
    }
}
