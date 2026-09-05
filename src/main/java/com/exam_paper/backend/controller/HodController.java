package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.service.HodService;
import com.exam_paper.backend.service.PacketService;
import com.exam_paper.backend.service.PacketTabService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
public class HodController {

    private final HodService hodService;
    private final PacketService packetService;
    private final PacketTabService packetTabService;

    // --- Statistics ---
    @GetMapping("/statistics")
    public HodDashboardDTO getStatistics(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return hodService.getDepartmentDashboard(username, null);
    }

    @GetMapping("/department/{deptId}/statistics")
    public HodDashboardDTO getDepartmentStatistics(
            @PathVariable String deptId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        return hodService.getDepartmentDashboard(username, parsedDeptId);
    }

    // --- Packets ---
    @GetMapping("/packets")
    public List<PacketDTO> getPackets(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return hodService.getDepartmentPackets(username, null);
    }

    @GetMapping("/department/{deptId}/packets")
    public List<PacketDTO> getDepartmentPackets(
            @PathVariable String deptId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        return hodService.getDepartmentPackets(username, parsedDeptId);
    }

    // --- Workload ---
    @GetMapping("/workload")
    public List<HodWorkloadDTO> getWorkload(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return hodService.getDepartmentWorkload(username, null);
    }

    @GetMapping("/department/{deptId}/workload")
    public List<HodWorkloadDTO> getDepartmentWorkload(
            @PathVariable String deptId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        return hodService.getDepartmentWorkload(username, parsedDeptId);
    }

    // --- Overdue ---
    @GetMapping("/overdue")
    public List<PacketDTO> getOverdue(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return hodService.getDepartmentOverdue(username, null);
    }

    @GetMapping("/department/{deptId}/overdue")
    public List<PacketDTO> getDepartmentOverdue(
            @PathVariable String deptId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        return hodService.getDepartmentOverdue(username, parsedDeptId);
    }

    // --- Previous Records ---
    @GetMapping("/previous-records")
    public List<PacketDTO> getPreviousRecords(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return hodService.getDepartmentPreviousRecords(username, null);
    }

    @GetMapping("/department/{deptId}/previous-records")
    public List<PacketDTO> getDepartmentPreviousRecords(
            @PathVariable String deptId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        return hodService.getDepartmentPreviousRecords(username, parsedDeptId);
    }

    // --- Reports ---
    @GetMapping("/report")
    public HodReportDTO getReport(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return hodService.getDepartmentReport(username, null);
    }

    @GetMapping("/department/{deptId}/report")
    public HodReportDTO getDepartmentReport(
            @PathVariable String deptId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        return hodService.getDepartmentReport(username, parsedDeptId);
    }

    // --- Export Report ---
    @GetMapping({"/report/export/{format}", "/department/{deptId}/report/export/{format}"})
    public void exportDepartmentReport(
            @PathVariable(required = false) String deptId,
            @PathVariable String format,
            Authentication authentication,
            HttpServletResponse response) throws IOException {
        String username = authentication != null ? authentication.getName() : null;
        Long parsedDeptId = parseDeptId(deptId);
        HodReportDTO report = hodService.getDepartmentReport(username, parsedDeptId);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=department_report_" + report.getDepartmentCode() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.println("Course Code,Course Name,Semester,Academic Year,Lecturer,Moderator,Status,Deadline,Overdue");

        for (HodReportDTO.CourseBreakdownDTO c : report.getCourseBreakdown()) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getSemester(),
                    c.getAcademicYear(),
                    c.getLecturerName(),
                    c.getModeratorName(),
                    c.getStatus(),
                    c.getDeadline(),
                    c.isOverdue() ? "YES" : "NO"
            );
        }
        writer.flush();
    }

    // --- Packet Details for HOD ---
    @GetMapping("/packet/{packetId}")
    public PacketDetailDTO getPacketDetail(@PathVariable Long packetId) {
        return packetService.getPacketDetail(packetId);
    }

    @GetMapping("/packet/{packetId}/comments")
    public List<CommentDTO> getPacketComments(@PathVariable Long packetId) {
        return packetTabService.getComments(packetId);
    }

    @PostMapping("/comment")
    public CommentDTO addComment(
            @RequestBody java.util.Map<String, Object> body,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "HOD";
        Long packetId = 1L;
        if (body.get("packetId") != null) {
            String pStr = String.valueOf(body.get("packetId"));
            if (pStr.contains("-")) {
                String[] parts = pStr.split("-");
                packetId = Long.parseLong(parts[parts.length - 1]);
            } else {
                packetId = Long.parseLong(pStr);
            }
        }
        String comment = body.get("commentText") != null
                ? String.valueOf(body.get("commentText"))
                : String.valueOf(body.getOrDefault("comment", ""));
        return packetTabService.addComment(packetId, comment, username);
    }

    // --- Notify Staff ---
    @PostMapping("/notify-staff")
    public void notifyStaff(
            @RequestBody HodNotifyStaffDTO dto,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "HOD";
        hodService.notifyStaff(username, dto);
    }

    private Long parseDeptId(String deptId) {
        if (deptId == null || deptId.equalsIgnoreCase("ALL") || deptId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(deptId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
