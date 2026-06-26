package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.DashboardSummaryDTO;
import com.exam_paper.backend.service.ARDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class ARDashboardController {
    private final ARDashboardService arDashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary() {
        return arDashboardService.getSummary();
    }
}
