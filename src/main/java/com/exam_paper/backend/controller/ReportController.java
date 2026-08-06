package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.ReportResponseDTO;
import com.exam_paper.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ReportResponseDTO getReport() {
        return reportService.getReport();
    }
}