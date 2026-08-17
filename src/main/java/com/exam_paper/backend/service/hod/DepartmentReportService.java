package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.dto.hod.DepartmentReportDto;
import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        StringBuilder pdfText = new StringBuilder();

        pdfText.append("=====================================================\n");
        pdfText.append("          DEPARTMENT ACADEMIC REPORT                 \n");
        pdfText.append("=====================================================\n\n");
        pdfText.append("Department ID: ").append(report.getDepartmentId()).append("\n");
        pdfText.append("Generated At: ").append(LocalDateTime.now()).append("\n\n");
        pdfText.append("SUMMARY STATISTICS:\n");
        pdfText.append("-----------------------------------------------------\n");
        pdfText.append("Total Exam Packets : ").append(report.getTotalPackets()).append("\n");
        pdfText.append("Pending Packets     : ").append(report.getPendingPackets()).append("\n");
        pdfText.append("In Progress Packets : ").append(report.getInProgressPackets()).append("\n");
        pdfText.append("Completed Packets   : ").append(report.getCompletedPackets()).append("\n");
        pdfText.append("Overdue Packets     : ").append(report.getOverduePackets()).append("\n\n");
        pdfText.append("LECTURER MARKING PROGRESS:\n");
        pdfText.append("-----------------------------------------------------\n");

        for (LecturerWorkloadDto w : report.getWorkloadDistribution()) {
            pdfText.append(String.format("- %s (%s): %d/%d Scripts Marked (%.1f%% Progress)\n",
                    w.getLecturerName(), w.getLecturerId(), w.getMarkedScripts(), w.getTotalScripts(), w.getProgressPercentage()));
        }

        return pdfText.toString().getBytes(StandardCharsets.UTF_8);
    }
}