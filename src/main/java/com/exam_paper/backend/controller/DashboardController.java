package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.DashboardResponseDTO;
import com.exam_paper.backend.dto.ModeratorDashboardResponseDTO;
import com.exam_paper.backend.service.DashboardService;
import com.exam_paper.backend.service.ModeratorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ModeratorDashboardService moderatorDashboardService;

    @GetMapping("/summary")
    public DashboardResponseDTO getDashboard(@RequestParam(required = false) String cycleId) {
        return dashboardService.getDashboard(cycleId);
    }

    @GetMapping("/moderator/summary")
    public ModeratorDashboardResponseDTO getModeratorDashboard(
            Authentication authentication,
            @RequestParam(required = false) String cycleId) {
        String username = authentication.getName();
        return moderatorDashboardService.getModeratorDashboard(username, cycleId);
    }
}