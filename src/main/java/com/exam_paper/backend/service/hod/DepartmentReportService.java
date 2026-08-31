package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.dto.hod.DepartmentReportDto;
import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
        long pending = packets.stream().filter(p -> "Pending".equalsIgnoreCase(p.getStatus())).count();
        long inProgress = packets.stream().filter(p -> "In Progress".equalsIgnoreCase(p.getStatus())).count();
        long completed = packets.stream().filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count();
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
        DepartmentReportDto report = generateDepartmentReport(deptId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Add content using iText layout elements
        document.add(new Paragraph("DEPARTMENT ACADEMIC REPORT").setBold().setFontSize(16));
        document.add(new Paragraph("Department ID: " + report.getDepartmentId()));
        document.add(new Paragraph("Generated At: " + LocalDateTime.now()));
        document.add(new Paragraph("\nSummary Statistics:").setBold());
        document.add(new Paragraph("- Total Exam Packets: " + report.getTotalPackets()));
        document.add(new Paragraph("- Pending Packets: " + report.getPendingPackets()));
        document.add(new Paragraph("- In Progress Packets: " + report.getInProgressPackets()));
        document.add(new Paragraph("- Completed Packets: " + report.getCompletedPackets()));
        document.add(new Paragraph("- Overdue Packets: " + report.getOverduePackets()));

        document.add(new Paragraph("\nLecturer Marking Progress:").setBold());

        Table table = new Table(new float[]{3, 2, 2, 2});
        table.addHeaderCell("Lecturer Name");
        table.addHeaderCell("Assigned");
        table.addHeaderCell("Marked");
        table.addHeaderCell("Progress");

        for (LecturerWorkloadDto w : report.getWorkloadDistribution()) {
            table.addCell(w.getLecturerName());
            table.addCell(String.valueOf(w.getTotalAssignedPackets()));
            table.addCell(String.valueOf(w.getMarkedScripts()));
            table.addCell(String.format("%.1f%%", w.getProgressPercentage()));
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }
}