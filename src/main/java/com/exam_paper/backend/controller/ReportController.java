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
    public ReportResponseDTO getReport(
            @RequestParam(defaultValue = "ALL") String semester) {
        return reportService.getReport(semester);
    }

    @GetMapping("/export/excel")
    public void exportExcel(
            @RequestParam(defaultValue = "ALL") String semester,
            jakarta.servlet.http.HttpServletResponse response)
            throws Exception {
        reportService.exportExcel(response, semester);
    }

    @GetMapping("/export/pdf")
    public void exportPdf(
            @RequestParam(defaultValue = "ALL") String semester,
            jakarta.servlet.http.HttpServletResponse response)
            throws Exception {
        reportService.exportPdf(response, semester);
    }
}