package com.exam_paper.backend.controller.hod;

import com.example.backend.dto.hod.*;
import com.example.backend.service.hod.HodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HodController {

    private final HodService hodService;

    // 1. View all department packets
    @GetMapping("/department/{deptId}/packets")
    public ResponseEntity<List<DepartmentPacketResponseDto>> getAllDepartmentPackets(@PathVariable String deptId) {
        return ResponseEntity.ok(hodService.getAllDepartmentPackets(deptId));
    }

    // 2. Search and filter department packets (by course, lecturer, moderator, status, cycle)
    @GetMapping("/department/{deptId}/packets/search")
    public ResponseEntity<List<DepartmentPacketResponseDto>> searchPackets(
            @PathVariable String deptId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cycleId,
            @RequestParam(required = false) String lecturerId) {
        return ResponseEntity.ok(hodService.filterAndSearchPackets(deptId, query, status, cycleId, lecturerId));
    }

    // 3. View full packet details, status, holder, and movement history
    @GetMapping("/packet/{packetId}")
    public ResponseEntity<PacketDetailDto> getPacketDetails(@PathVariable String packetId) {
        return ResponseEntity.ok(hodService.getPacketDetails(packetId));
    }

    // 4. Access previous academic cycle records
    @GetMapping("/department/{deptId}/previous-records")
    public ResponseEntity<List<DepartmentPacketResponseDto>> getPreviousRecords(@PathVariable String deptId) {
        return ResponseEntity.ok(hodService.getPreviousCycleRecords(deptId));
    }

    // 5. Explicitly monitor delayed and overdue tasks
    @GetMapping("/department/{deptId}/overdue")
    public ResponseEntity<List<DepartmentPacketResponseDto>> getOverduePackets(@PathVariable String deptId) {
        return ResponseEntity.ok(hodService.getOverduePackets(deptId));
    }

    // 6. Monitor lecturer workload, marking progress & compare workload distribution
    @GetMapping("/department/{deptId}/workload")
    public ResponseEntity<List<LecturerWorkloadDto>> getDepartmentWorkload(@PathVariable String deptId) {
        return ResponseEntity.ok(hodService.getDepartmentWorkload(deptId));
    }

    // 7. Add comment/feedback on packet
    @PostMapping("/comment")
    public ResponseEntity<CommentResponseDto> addComment(@RequestBody CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok(hodService.addComment(commentRequestDto));
    }

    // 8. View packet comments / communication history
    @GetMapping("/packet/{packetId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getPacketComments(@PathVariable String packetId) {
        return ResponseEntity.ok(hodService.getPacketComments(packetId));
    }

    // 9. Department reports and analytics
    @GetMapping("/department/{deptId}/report")
    public ResponseEntity<DepartmentReportDto> getDepartmentReport(@PathVariable String deptId) {
        return ResponseEntity.ok(hodService.generateDepartmentReport(deptId));
    }

    // 10. Export report as Excel/CSV
    @GetMapping("/department/{deptId}/report/export/excel")
    public ResponseEntity<byte[]> exportReportExcel(@PathVariable String deptId) {
        byte[] excelData = hodService.exportDepartmentReportExcel(deptId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report_" + deptId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(excelData);
    }

    // 11. Export report as PDF
    @GetMapping("/department/{deptId}/report/export/pdf")
    public ResponseEntity<byte[]> exportReportPdf(@PathVariable String deptId) {
        byte[] pdfData = hodService.exportDepartmentReportPdf(deptId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report_" + deptId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}