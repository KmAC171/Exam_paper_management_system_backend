package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.dto.hod.DepartmentReportDto;
import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentReportService {

    private final PacketWorkflowService packetWorkflowService;
    private final OverdueTrackingService overdueTrackingService;
    private final WorkloadService workloadService;

    public DepartmentReportDto generateDepartmentReport(String deptId) {
        List<DepartmentPacketResponseDto> packets = packetWorkflowService.getAllDepartmentPackets(deptId);

        long total = packets.size();
        long pending = packets.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
        long inProgress = packets.stream().filter(p -> !"COMPLETED".equalsIgnoreCase(p.getStatus()) && !"APPROVED".equalsIgnoreCase(p.getStatus()) && !"PENDING".equalsIgnoreCase(p.getStatus())).count();
        long completed = packets.stream().filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()) || "APPROVED".equalsIgnoreCase(p.getStatus())).count();
        long overdue = overdueTrackingService.getOverduePackets(deptId).size();

        List<LecturerWorkloadDto> workloads = workloadService.getDepartmentWorkload(deptId);

        return DepartmentReportDto.builder()
                .departmentId(deptId)
                .departmentName(deptId)
                .totalPackets(total)
                .pendingPackets(pending)
                .inProgressPackets(inProgress)
                .completedPackets(completed)
                .overduePackets(overdue)
                .workloadDistribution(workloads)
                .build();
    }

    public byte[] exportDepartmentReportExcel(String deptId) {
        DepartmentReportDto report = generateDepartmentReport(deptId);
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("DEPARTMENT PERFORMANCE AND PROGRESS REPORT\n");
        csvBuilder.append("Department ID,").append(report.getDepartmentId()).append("\n");
        csvBuilder.append("Total Packets,").append(report.getTotalPackets()).append("\n");
        csvBuilder.append("Pending,").append(report.getPendingPackets()).append("\n");
        csvBuilder.append("In Progress,").append(report.getInProgressPackets()).append("\n");
        csvBuilder.append("Completed,").append(report.getCompletedPackets()).append("\n");
        csvBuilder.append("Overdue,").append(report.getOverduePackets()).append("\n\n");

        csvBuilder.append("LECTURER WORKLOAD & MARKING PROGRESS\n");
        csvBuilder.append("Lecturer ID,Lecturer Name,Assigned Packets,Total Scripts,Marked Scripts,Progress %\n");

        for (LecturerWorkloadDto w : report.getWorkloadDistribution()) {
            csvBuilder.append(String.format("%s,\"%s\",%d,%d,%d,%.2f%%\n",
                    w.getLecturerId(), w.getLecturerName(), w.getTotalAssignedPackets(),
                    w.getTotalScripts(), w.getMarkedScripts(), w.getProgressPercentage()));
        }
        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportDepartmentReportPdf(String deptId) {
        try {
            DepartmentReportDto report = generateDepartmentReport(deptId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 10);

            Paragraph title = new Paragraph("DEPARTMENT ACADEMIC REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            document.add(new Paragraph("Department: " + report.getDepartmentName(), bodyFont));
            document.add(new Paragraph("Generated At: " + LocalDateTime.now(), bodyFont));
            document.add(new Paragraph("\nSummary Statistics:", headFont));
            document.add(new Paragraph("- Total Exam Packets: " + report.getTotalPackets(), bodyFont));
            document.add(new Paragraph("- Pending Packets: " + report.getPendingPackets(), bodyFont));
            document.add(new Paragraph("- In Progress Packets: " + report.getInProgressPackets(), bodyFont));
            document.add(new Paragraph("- Completed Packets: " + report.getCompletedPackets(), bodyFont));
            document.add(new Paragraph("- Overdue Packets: " + report.getOverduePackets(), bodyFont));

            document.add(new Paragraph("\nLecturer Marking Progress:\n\n", headFont));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            String[] headers = {"Lecturer Name", "Assigned", "Marked Scripts", "Progress"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
                cell.setBackgroundColor(new BaseColor(124, 77, 255));
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (LecturerWorkloadDto w : report.getWorkloadDistribution()) {
                table.addCell(new Phrase(w.getLecturerName(), bodyFont));
                table.addCell(new Phrase(String.valueOf(w.getTotalAssignedPackets()), bodyFont));
                table.addCell(new Phrase(String.valueOf(w.getMarkedScripts()), bodyFont));
                table.addCell(new Phrase(String.format("%.1f%%", w.getProgressPercentage()), bodyFont));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }
}
