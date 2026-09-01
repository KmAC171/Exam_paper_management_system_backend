package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.repository.DelayReasonRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExamPacketRepository examPacketRepository;
    private final DelayReasonRepository delayReasonRepository;

    private static final String[] MONTH_NAMES = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // semester → [startMonth, endMonth] (0 = no filter)
    private int[] getSemesterRange(String semester) {
        return switch (semester) {
            case "SEM1" -> new int[]{1, 6};
            case "SEM2" -> new int[]{7, 12};
            default -> new int[]{0, 0}; // ALL TIME
        };
    }

    public ReportResponseDTO getReport(String semester) {
        int[] range = getSemesterRange(semester);
        int startMonth = range[0];
        int endMonth = range[1];
        LocalDate today = LocalDate.now();

        // KPI
        long total = examPacketRepository.countFiltered(startMonth, endMonth);
        long completed = examPacketRepository.countCompletedFiltered(startMonth, endMonth);
        long delayed = examPacketRepository.countDelayedFiltered(startMonth, endMonth);
        long onTime = total - delayed;

        double completionRate = total > 0
                ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0;
        double onTimeRate = total > 0
                ? Math.round((onTime * 100.0 / total) * 10.0) / 10.0 : 0;
        double avgProcessingDays = 4.8;

        ReportKpiDTO kpi = new ReportKpiDTO(
                completionRate, avgProcessingDays, onTimeRate, delayed);

        // Monthly trend
        List<MonthlyTrendDTO> monthlyTrend = examPacketRepository
                .getMonthlyTrendFiltered(startMonth, endMonth)
                .stream()
                .map(p -> new MonthlyTrendDTO(
                        MONTH_NAMES[p.getMonth()],
                        p.getSubmitted(),
                        p.getApproved(),
                        p.getDelayed()))
                .collect(Collectors.toList());

        // Delay reasons
        List<Object[]> reasonCounts = delayReasonRepository.countByReason();
        long totalReasons = reasonCounts.stream()
                .mapToLong(r -> (Long) r[1]).sum();
        List<DelayReasonDTO> delayReasons = reasonCounts.stream()
                .map(r -> {
                    long count = (Long) r[1];
                    double pct = totalReasons > 0
                            ? Math.round((count * 100.0 / totalReasons) * 10.0) / 10.0
                            : 0;
                    return new DelayReasonDTO((String) r[0], count, pct);
                })
                .collect(Collectors.toList());

        // Department comparison
        List<DepartmentReportDTO> departments = examPacketRepository
                .getDepartmentReport()
                .stream()
                .map(p -> {
                    double deptOnTimeRate = p.getTotalPackets() > 0
                            ? Math.round((p.getOnTime() * 100.0 / p.getTotalPackets()) * 10.0) / 10.0
                            : 0;
                    return new DepartmentReportDTO(
                            p.getDepartmentName(),
                            p.getTotalPackets(),
                            p.getOnTime(),
                            p.getDelayed(),
                            deptOnTimeRate);
                })
                .collect(Collectors.toList());

        return new ReportResponseDTO(kpi, monthlyTrend, delayReasons, departments);
    }

    // ── EXPORT EXCEL ──────────────────────────────────────────

    public void exportExcel(HttpServletResponse response, String semester) throws IOException {
        ReportResponseDTO report = getReport(semester != null ? semester : "ALL");

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=exam-report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {

            // ── KPI Sheet ──
            Sheet kpiSheet = workbook.createSheet("KPI Summary");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row kpiHeader = kpiSheet.createRow(0);
            String[] kpiCols = {"Metric", "Value"};
            for (int i = 0; i < kpiCols.length; i++) {
                Cell cell = kpiHeader.createCell(i);
                cell.setCellValue(kpiCols[i]);
                cell.setCellStyle(headerStyle);
            }
            String[][] kpiData = {
                    {"Overall Completion Rate", report.getKpi().getCompletionRate() + "%"},
                    {"Avg. Processing Time", report.getKpi().getAvgProcessingDays() + " days"},
                    {"On-Time Submission Rate", report.getKpi().getOnTimeSubmissionRate() + "%"},
                    {"Packets in Delay", String.valueOf(report.getKpi().getPacketsInDelay())},
            };
            for (int i = 0; i < kpiData.length; i++) {
                Row row = kpiSheet.createRow(i + 1);
                row.createCell(0).setCellValue(kpiData[i][0]);
                row.createCell(1).setCellValue(kpiData[i][1]);
            }
            kpiSheet.autoSizeColumn(0);
            kpiSheet.autoSizeColumn(1);

            // ── Department Sheet ──
            Sheet deptSheet = workbook.createSheet("Department Comparison");
            Row deptHeader = deptSheet.createRow(0);
            String[] deptCols = {"Department", "Total Packets", "On Time", "Delayed", "On-Time Rate"};
            for (int i = 0; i < deptCols.length; i++) {
                Cell cell = deptHeader.createCell(i);
                cell.setCellValue(deptCols[i]);
                cell.setCellStyle(headerStyle);
            }
            int deptRow = 1;
            for (DepartmentReportDTO d : report.getDepartmentComparison()) {
                Row row = deptSheet.createRow(deptRow++);
                row.createCell(0).setCellValue(d.getDepartment());
                row.createCell(1).setCellValue(d.getTotalPackets());
                row.createCell(2).setCellValue(d.getOnTime());
                row.createCell(3).setCellValue(d.getDelayed());
                row.createCell(4).setCellValue(d.getOnTimeRate() + "%");
            }
            for (int i = 0; i < deptCols.length; i++) deptSheet.autoSizeColumn(i);

            // ── Monthly Trend Sheet ──
            Sheet trendSheet = workbook.createSheet("Monthly Trend");
            Row trendHeader = trendSheet.createRow(0);
            String[] trendCols = {"Month", "Submitted", "Approved", "Delayed"};
            for (int i = 0; i < trendCols.length; i++) {
                Cell cell = trendHeader.createCell(i);
                cell.setCellValue(trendCols[i]);
                cell.setCellStyle(headerStyle);
            }
            int trendRow = 1;
            for (MonthlyTrendDTO m : report.getMonthlyTrend()) {
                Row row = trendSheet.createRow(trendRow++);
                row.createCell(0).setCellValue(m.getMonth());
                row.createCell(1).setCellValue(m.getSubmitted());
                row.createCell(2).setCellValue(m.getApproved());
                row.createCell(3).setCellValue(m.getDelayed());
            }
            for (int i = 0; i < trendCols.length; i++) trendSheet.autoSizeColumn(i);

            workbook.write(response.getOutputStream());
        }
    }

    // ── EXPORT PDF ────────────────────────────────────────────

    public void exportPdf(HttpServletResponse response, String semester)
            throws IOException, DocumentException {
        ReportResponseDTO report = getReport(semester != null ? semester : "ALL");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=exam-report.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 18,
                        com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headFont =
                new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 13,
                        com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font bodyFont =
                new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        // Title
        Paragraph title = new Paragraph("Exam Packet Management Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // KPI Section
        document.add(new Paragraph("KPI Summary", headFont));
        document.add(Chunk.NEWLINE);
        PdfPTable kpiTable = new PdfPTable(2);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingAfter(20);
        addTableHeader(kpiTable, new String[]{"Metric", "Value"});
        addTableRow(kpiTable, "Overall Completion Rate",
                report.getKpi().getCompletionRate() + "%", bodyFont);
        addTableRow(kpiTable, "Avg. Processing Time",
                report.getKpi().getAvgProcessingDays() + " days", bodyFont);
        addTableRow(kpiTable, "On-Time Submission Rate",
                report.getKpi().getOnTimeSubmissionRate() + "%", bodyFont);
        addTableRow(kpiTable, "Packets in Delay",
                String.valueOf(report.getKpi().getPacketsInDelay()), bodyFont);
        document.add(kpiTable);

        // Department Section
        document.add(new Paragraph("Department Comparison", headFont));
        document.add(Chunk.NEWLINE);
        PdfPTable deptTable = new PdfPTable(5);
        deptTable.setWidthPercentage(100);
        deptTable.setSpacingAfter(20);
        addTableHeader(deptTable, new String[]{
                "Department", "Total", "On Time", "Delayed", "Rate"});
        for (DepartmentReportDTO d : report.getDepartmentComparison()) {
            deptTable.addCell(new Phrase(d.getDepartment(), bodyFont));
            deptTable.addCell(new Phrase(String.valueOf(d.getTotalPackets()), bodyFont));
            deptTable.addCell(new Phrase(String.valueOf(d.getOnTime()), bodyFont));
            deptTable.addCell(new Phrase(String.valueOf(d.getDelayed()), bodyFont));
            deptTable.addCell(new Phrase(d.getOnTimeRate() + "%", bodyFont));
        }
        document.add(deptTable);

        // Monthly Trend Section
        document.add(new Paragraph("Monthly Submission Trend", headFont));
        document.add(Chunk.NEWLINE);
        PdfPTable trendTable = new PdfPTable(4);
        trendTable.setWidthPercentage(100);
        addTableHeader(trendTable, new String[]{
                "Month", "Submitted", "Approved", "Delayed"});
        for (MonthlyTrendDTO m : report.getMonthlyTrend()) {
            trendTable.addCell(new Phrase(m.getMonth(), bodyFont));
            trendTable.addCell(new Phrase(String.valueOf(m.getSubmitted()), bodyFont));
            trendTable.addCell(new Phrase(String.valueOf(m.getApproved()), bodyFont));
            trendTable.addCell(new Phrase(String.valueOf(m.getDelayed()), bodyFont));
        }
        document.add(trendTable);

        document.close();
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        com.itextpdf.text.Font f =
                new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                        com.itextpdf.text.Font.BOLD,
                        BaseColor.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, f));
            cell.setBackgroundColor(new BaseColor(124, 77, 255));
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String label,
                             String value,
                             com.itextpdf.text.Font font) {
        table.addCell(new Phrase(label, font));
        table.addCell(new Phrase(value, font));
    }
}