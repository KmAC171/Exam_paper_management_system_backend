package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.LecturerDashboardResponseDTO;
import com.exam_paper.backend.service.lecturer.LecturerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LecturerDashboardController {

    private final LecturerDashboardService lecturerDashboardService;

    @GetMapping("/{lecturerId}/dashboard")
    public ResponseEntity<LecturerDashboardResponseDTO> getDashboard(@PathVariable String lecturerId) {
        LecturerDashboardResponseDTO dashboard = lecturerDashboardService.getDashboard(lecturerId);
        return ResponseEntity.ok(dashboard);
    }
}
