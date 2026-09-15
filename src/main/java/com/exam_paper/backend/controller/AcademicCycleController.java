package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.AcademicCycleDTO;
import com.exam_paper.backend.dto.SemesterRolloverDTO.*;
import com.exam_paper.backend.service.AcademicCycleService;
import com.exam_paper.backend.service.SemesterRolloverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class AcademicCycleController {

    private final AcademicCycleService academicCycleService;
    private final SemesterRolloverService semesterRolloverService;

    @GetMapping
    public ResponseEntity<List<AcademicCycleDTO>> getAllCycles() {
        return ResponseEntity.ok(academicCycleService.getAllCycles());
    }

    @GetMapping("/active")
    public ResponseEntity<AcademicCycleDTO> getActiveCycle() {
        return ResponseEntity.ok(academicCycleService.getActiveCycle());
    }

    @GetMapping("/{cycleId}")
    public ResponseEntity<AcademicCycleDTO> getCycleById(@PathVariable String cycleId) {
        return ResponseEntity.ok(academicCycleService.getCycleById(cycleId));
    }

    @PostMapping
    public ResponseEntity<?> createCycle(@RequestBody AcademicCycleDTO dto) {
        try {
            AcademicCycleDTO created = academicCycleService.createCycle(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to create cycle."));
        }
    }

    @PutMapping("/{cycleId}/activate")
    public ResponseEntity<?> activateCycle(@PathVariable String cycleId) {
        try {
            AcademicCycleDTO activated = academicCycleService.activateCycle(cycleId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to activate cycle."));
        }
    }

    @PutMapping("/{cycleId}/status")
    public ResponseEntity<?> updateCycleStatus(@PathVariable String cycleId, @RequestParam String status) {
        try {
            AcademicCycleDTO updated = academicCycleService.updateCycleStatus(cycleId, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to update cycle status."));
        }
    }

    @PostMapping("/rollover/preview")
    public ResponseEntity<?> previewRollover(@RequestBody RolloverRequest request) {
        try {
            RolloverPreviewResponse preview = semesterRolloverService.previewRollover(request);
            return ResponseEntity.ok(preview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to preview rollover."));
        }
    }

    @PostMapping("/rollover/execute")
    public ResponseEntity<?> executeRollover(@RequestBody RolloverRequest request, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "System Admin";
            RolloverExecutionResult result = semesterRolloverService.executeRollover(request, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to execute rollover."));
        }
    }
}
