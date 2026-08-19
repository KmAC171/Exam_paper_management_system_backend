package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.DashboardResponseDTO;
import com.exam_paper.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardResponseDTO getDashboard() {
        return dashboardService.getDashboard();
    }
}