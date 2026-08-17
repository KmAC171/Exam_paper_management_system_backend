package com.exam_paper.backend.controller.hod;

import com.exam_paper.backend.dto.hod.DepartmentReportDto;
import com.exam_paper.backend.service.hod.DepartmentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentReportController {

    private final DepartmentReportService departmentReportService;

    @GetMapping("/department/{deptId}/report")
    public ResponseEntity<DepartmentReportDto> getDepartmentReport(@PathVariable String deptId) {
        return ResponseEntity.ok(departmentReportService.generateDepartmentReport(deptId));
    }

    @GetMapping("/department/{deptId}/report/export/excel")
    public ResponseEntity<byte[]> exportReportExcel(@PathVariable String deptId) {
        byte[] excelData = departmentReportService.exportDepartmentReportExcel(deptId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report_" + deptId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(excelData);
    }

    @GetMapping("/department/{deptId}/report/export/pdf")
    public ResponseEntity<byte[]> exportReportPdf(@PathVariable String deptId) {
        byte[] pdfData = departmentReportService.exportDepartmentReportPdf(deptId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report_" + deptId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}