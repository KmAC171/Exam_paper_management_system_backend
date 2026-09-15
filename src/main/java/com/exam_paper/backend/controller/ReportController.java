package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.MultiCycleTrendDTO;
import com.exam_paper.backend.dto.ReportResponseDTO;
import com.exam_paper.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ReportResponseDTO getReport(
            @RequestParam(defaultValue = "ALL", required = false) String semester,
            @RequestParam(required = false) String cycleId) {
        String query = cycleId != null && !cycleId.trim().isEmpty() ? cycleId : semester;
        return reportService.getReport(query);
    }

    @GetMapping("/multi-cycle-trends")
    public ResponseEntity<MultiCycleTrendDTO> getMultiCycleTrends() {
        return ResponseEntity.ok(reportService.getMultiCycleTrends());
    }

    @GetMapping("/export/excel")
    public void exportExcel(
            @RequestParam(defaultValue = "ALL", required = false) String semester,
            @RequestParam(required = false) String cycleId,
            jakarta.servlet.http.HttpServletResponse response)
            throws Exception {
        String query = cycleId != null && !cycleId.trim().isEmpty() ? cycleId : semester;
        reportService.exportExcel(response, query);
    }

    @GetMapping("/export/pdf")
    public void exportPdf(
            @RequestParam(defaultValue = "ALL", required = false) String semester,
            @RequestParam(required = false) String cycleId,
            jakarta.servlet.http.HttpServletResponse response)
            throws Exception {
        String query = cycleId != null && !cycleId.trim().isEmpty() ? cycleId : semester;
        reportService.exportPdf(response, query);
    }
}