package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.AcademicCycle;
import com.exam_paper.backend.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExamPacketRepository examPacketRepository;
    private final DelayReasonRepository delayReasonRepository;
    private final AcademicCycleRepository academicCycleRepository;
    private final AcademicCycleService academicCycleService;

    private static final String[] MONTH_NAMES = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public ReportResponseDTO getReport(String cycleIdOrSemester) {
        String targetCycleId = null;

        String cleaned = PacketService.cleanCycleId(cycleIdOrSemester);
        if (cleaned != null && !cleaned.isEmpty() && !"ALL".equalsIgnoreCase(cleaned)) {
            // Check if it's an existing cycle ID
            if (academicCycleRepository.existsByCycleId(cleaned)) {
                targetCycleId = cleaned;
            } else if ("SEM1".equalsIgnoreCase(cleaned) || "SEM2".equalsIgnoreCase(cleaned)) {
                // Find matching semester cycle
                int sem = "SEM1".equalsIgnoreCase(cleaned) ? 1 : 2;
                List<AcademicCycle> cycles = academicCycleRepository.findAllByOrderByStartDateDesc();
                for (AcademicCycle c : cycles) {
                    if (c.getSemester() != null && c.getSemester() == sem) {
                        targetCycleId = c.getCycleId();
                        break;
                    }
                }
            } else {
                targetCycleId = cleaned;
            }
        }

        // KPI
        long total = examPacketRepository.countByCycle(targetCycleId);
        long completed = examPacketRepository.countCompletedByCycle(targetCycleId);
        long delayed = examPacketRepository.countDelayedByCycle(targetCycleId);
        long onTime = Math.max(0, total - delayed);

        double completionRate = total > 0
                ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0;
        double onTimeRate = total > 0
                ? Math.round((onTime * 100.0 / total) * 10.0) / 10.0 : 0.0;
        double avgProcessingDays = 4.8;

        ReportKpiDTO kpi = new ReportKpiDTO(
                completionRate, avgProcessingDays, onTimeRate, delayed);

        // Monthly trend
        List<MonthlyTrendDTO> monthlyTrend = examPacketRepository
                .getMonthlyTrendByCycle(targetCycleId)
                .stream()
                .filter(p -> p != null && p.getMonth() != null)
                .map(p -> {
                    int m = p.getMonth();
                    int monthIdx = m >= 1 && m <= 12 ? m : 1;
                    return new MonthlyTrendDTO(
                            MONTH_NAMES[monthIdx],
                            p.getSubmitted() != null ? p.getSubmitted() : 0L,
                            p.getApproved() != null ? p.getApproved() : 0L,
                            p.getDelayed() != null ? p.getDelayed() : 0L);
                })
                .collect(Collectors.toList());

        // Delay reasons
        List<Object[]> reasonCounts = delayReasonRepository.countByReason();
        long totalReasons = 0;
        if (reasonCounts != null) {
            for (Object[] r : reasonCounts) {
                if (r != null && r.length > 1 && r[1] instanceof Number) {
                    totalReasons += ((Number) r[1]).longValue();
                }
            }
        }
        final long finalTotalReasons = totalReasons;
        List<DelayReasonDTO> delayReasons = reasonCounts != null ? reasonCounts.stream()
                .filter(r -> r != null && r.length > 0)
                .map(r -> {
                    String reason = r[0] != null ? r[0].toString() : "Other";
                    long count = (r.length > 1 && r[1] instanceof Number) ? ((Number) r[1]).longValue() : 0L;
                    double pct = finalTotalReasons > 0
                            ? Math.round((count * 100.0 / finalTotalReasons) * 10.0) / 10.0
                            : 0.0;
                    return new DelayReasonDTO(reason, count, pct);
                })
                .collect(Collectors.toList()) : new java.util.ArrayList<>();

        // Department comparison
        List<DepartmentReportDTO> departments = examPacketRepository
                .getDepartmentReportByCycle(targetCycleId)
                .stream()
                .filter(p -> p != null)
                .map(p -> {
                    long totalPkt = p.getTotalPackets() != null ? p.getTotalPackets() : 0L;
                    long onTimePkt = p.getOnTime() != null ? p.getOnTime() : 0L;
                    long delayedPkt = p.getDelayed() != null ? p.getDelayed() : 0L;
                    double deptOnTimeRate = totalPkt > 0
                            ? Math.round((onTimePkt * 100.0 / totalPkt) * 10.0) / 10.0
                            : 0.0;
                    return new DepartmentReportDTO(
                            p.getDepartmentName() != null ? p.getDepartmentName() : "Unknown",
                            totalPkt,
                            onTimePkt,
                            delayedPkt,
                            deptOnTimeRate);
                })
                .collect(Collectors.toList());

        return new ReportResponseDTO(kpi, monthlyTrend, delayReasons, departments);
    }

    public MultiCycleTrendDTO getMultiCycleTrends() {
        List<AcademicCycle> cycles = academicCycleRepository.findAllByOrderByStartDateDesc();
        List<MultiCycleTrendDTO.CycleSummaryDTO> summaries = new java.util.ArrayList<>();
        List<MultiCycleTrendDTO.DepartmentTrendDTO> deptTrends = new java.util.ArrayList<>();
        List<MultiCycleTrendDTO.SubmissionPatternDTO> patterns = new java.util.ArrayList<>();

        if (cycles != null) {
            for (AcademicCycle c : cycles) {
                if (c == null || c.getCycleId() == null) continue;
                long total = examPacketRepository.countByCycle(c.getCycleId());
                long completed = examPacketRepository.countCompletedByCycle(c.getCycleId());
                long delayed = examPacketRepository.countDelayedByCycle(c.getCycleId());
                long onTime = Math.max(0, total - delayed);
                double completionRate = total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0;
                double onTimeRate = total > 0 ? Math.round((onTime * 100.0 / total) * 10.0) / 10.0 : 0.0;

                summaries.add(MultiCycleTrendDTO.CycleSummaryDTO.builder()
                        .cycleId(c.getCycleId())
                        .cycleName(c.getCycleName() != null ? c.getCycleName() : c.getCycleId())
                        .academicYear(c.getAcademicYear())
                        .semester(c.getSemester())
                        .totalPackets(total)
                        .completedPackets(completed)
                        .delayedPackets(delayed)
                        .onTimeRate(onTimeRate)
                        .completionRate(completionRate)
                        .avgProcessingDays(4.8)
                        .build());

                // Dept breakdowns
                List<DepartmentReportProjection> deptReports = examPacketRepository.getDepartmentReportByCycle(c.getCycleId());
                if (deptReports != null) {
                    deptReports.forEach(p -> {
                        if (p != null) {
                            long totalPkt = p.getTotalPackets() != null ? p.getTotalPackets() : 0L;
                            long onTimePkt = p.getOnTime() != null ? p.getOnTime() : 0L;
                            long delayedPkt = p.getDelayed() != null ? p.getDelayed() : 0L;
                            double deptRate = totalPkt > 0
                                    ? Math.round((onTimePkt * 100.0 / totalPkt) * 10.0) / 10.0
                                    : 0.0;
                            deptTrends.add(MultiCycleTrendDTO.DepartmentTrendDTO.builder()
                                    .departmentName(p.getDepartmentName() != null ? p.getDepartmentName() : "Unknown")
                                    .cycleId(c.getCycleId())
                                    .cycleName(c.getCycleName())
                                    .totalPackets(totalPkt)
                                    .onTime(onTimePkt)
                                    .delayed(delayedPkt)
                                    .onTimeRate(deptRate)
                                    .build());
                        }
                    });
                }

                // Trend patterns
                List<MonthlyTrendProjection> monthlyTrends = examPacketRepository.getMonthlyTrendByCycle(c.getCycleId());
                if (monthlyTrends != null) {
                    monthlyTrends.forEach(m -> {
                        if (m != null && m.getMonth() != null) {
                            int mVal = m.getMonth();
                            int monthIdx = mVal >= 1 && mVal <= 12 ? mVal : 1;
                            patterns.add(MultiCycleTrendDTO.SubmissionPatternDTO.builder()
                                    .cycleId(c.getCycleId())
                                    .cycleName(c.getCycleName())
                                    .periodLabel(MONTH_NAMES[monthIdx])
                                    .submittedCount(m.getSubmitted() != null ? m.getSubmitted() : 0L)
                                    .approvedCount(m.getApproved() != null ? m.getApproved() : 0L)
                                    .build());
                        }
                    });
                }
            }
        }

        return MultiCycleTrendDTO.builder()
                .cycleSummaries(summaries)
                .departmentTrends(deptTrends)
                .submissionPatterns(patterns)
                .build();
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

    // ── EXPORT PDF (ENHANCED BLACK & WHITE / MONOCHROME) ────

    public void exportPdf(HttpServletResponse response, String semester)
            throws IOException, DocumentException {
        ReportResponseDTO report = getReport(semester != null ? semester : "ALL");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=exam-report.pdf");

        // Page setup: A4 with 36pt margins, bottom 45pt to accommodate footer
        Document document = new Document(PageSize.A4, 36, 36, 36, 45);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new PdfFooterPageEvent());
        document.open();

        // ── Color Palette (Black, White & Grayscale) ──
        BaseColor colorDark = new BaseColor(33, 37, 41);          // #212529 Deep Charcoal / Primary Text
        BaseColor colorDarkHeader = new BaseColor(40, 44, 52);    // #282C34 Table Header Dark
        BaseColor colorSubtleBg = new BaseColor(248, 249, 250);   // #F8F9FA Subtle Row / Card Background
        BaseColor colorSummaryBg = new BaseColor(233, 236, 239);  // #E9ECEF Summary / Totals Row
        BaseColor colorBorder = new BaseColor(218, 222, 229);     // #DADEE5 Clean Borders
        BaseColor colorMetaBg = new BaseColor(245, 247, 250);     // #F5F7FA Metadata Box
        BaseColor colorMutedText = new BaseColor(90, 95, 105);    // #5A5F69 Muted Text

        // ── Typography ──
        com.itextpdf.text.Font superTitleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8.5f,
                com.itextpdf.text.Font.BOLD, colorMutedText);
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                com.itextpdf.text.Font.BOLD, colorDark);
        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10.5f,
                com.itextpdf.text.Font.BOLD, colorDark);
        com.itextpdf.text.Font tableHeadFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8.5f,
                com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        com.itextpdf.text.Font bodyFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8.5f,
                com.itextpdf.text.Font.NORMAL, colorDark);
        com.itextpdf.text.Font bodyBoldFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8.5f,
                com.itextpdf.text.Font.BOLD, colorDark);
        com.itextpdf.text.Font kpiValFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 13,
                com.itextpdf.text.Font.BOLD, colorDark);
        com.itextpdf.text.Font metaLabelFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                com.itextpdf.text.Font.BOLD, colorDark);
        com.itextpdf.text.Font metaValueFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                com.itextpdf.text.Font.NORMAL, colorDark);

        // Determine cycle label
        String cycleLabel = "All Academic Cycles";
        if (semester != null && !semester.trim().isEmpty() && !"ALL".equalsIgnoreCase(semester)) {
            String cleaned = PacketService.cleanCycleId(semester);
            if (cleaned != null) {
                AcademicCycle ac = academicCycleRepository.findByCycleId(cleaned).orElse(null);
                if (ac != null && ac.getCycleName() != null && !ac.getCycleName().trim().isEmpty()) {
                    cycleLabel = ac.getCycleName() + " (" + ac.getCycleId() + ")";
                } else {
                    cycleLabel = cleaned;
                }
            }
        }

        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // ── 1. Document Header & Title ──
        Paragraph superTitle = new Paragraph("EXAM PACKET MANAGEMENT SYSTEM", superTitleFont);
        superTitle.setAlignment(Element.ALIGN_CENTER);
        superTitle.setSpacingAfter(2);
        document.add(superTitle);

        Paragraph mainTitle = new Paragraph("EXAMINATION CYCLE AUDIT & PERFORMANCE REPORT", titleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        mainTitle.setSpacingAfter(10);
        document.add(mainTitle);

        // Subtle Divider Line
        LineSeparator divider = new LineSeparator(1f, 100, colorBorder, Element.ALIGN_CENTER, -2);
        document.add(divider);
        document.add(Chunk.NEWLINE);

        // ── 2. Metadata Grid (Key-Value 2x2 Box) ──
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{50, 50});
        metaTable.setSpacingAfter(14);

        PdfPCell mCell1 = createMetaCell("ACADEMIC CYCLE / SCOPE", cycleLabel, metaLabelFont, metaValueFont, colorMetaBg, colorBorder);
        PdfPCell mCell2 = createMetaCell("GENERATED DATE & TIME", generatedAt, metaLabelFont, metaValueFont, colorMetaBg, colorBorder);
        PdfPCell mCell3 = createMetaCell("GENERATED BY", "System Administrator", metaLabelFont, metaValueFont, colorMetaBg, colorBorder);
        PdfPCell mCell4 = createMetaCell("REPORT SCOPE", "Departmental Performance & Packet Distribution", metaLabelFont, metaValueFont, colorMetaBg, colorBorder);

        metaTable.addCell(mCell1);
        metaTable.addCell(mCell2);
        metaTable.addCell(mCell3);
        metaTable.addCell(mCell4);
        document.add(metaTable);

        // ── 3. KPI Summary Cards ──
        Paragraph kpiHeading = new Paragraph("1. KEY PERFORMANCE INDICATORS (KPI SUMMARY)", sectionFont);
        kpiHeading.setSpacingAfter(6);
        document.add(kpiHeading);

        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{25, 25, 25, 25});
        kpiTable.setSpacingAfter(16);

        addPdfTableHeader(kpiTable, new String[]{
                "Completion Rate", "Avg. Processing", "On-Time Rate", "Delayed Packets"
        }, tableHeadFont, colorDarkHeader, colorBorder);

        addKpiValueCell(kpiTable, report.getKpi().getCompletionRate() + "%", kpiValFont, colorSubtleBg, colorBorder);
        addKpiValueCell(kpiTable, report.getKpi().getAvgProcessingDays() + " Days", kpiValFont, colorSubtleBg, colorBorder);
        addKpiValueCell(kpiTable, report.getKpi().getOnTimeSubmissionRate() + "%", kpiValFont, colorSubtleBg, colorBorder);
        addKpiValueCell(kpiTable, String.valueOf(report.getKpi().getPacketsInDelay()), kpiValFont, colorSubtleBg, colorBorder);

        document.add(kpiTable);

        // ── 4. Enhanced Department Comparison Table ──
        Paragraph deptHeading = new Paragraph("2. DEPARTMENTAL PERFORMANCE & PACKET DISTRIBUTION", sectionFont);
        deptHeading.setSpacingAfter(6);
        document.add(deptHeading);

        PdfPTable deptTable = new PdfPTable(6);
        deptTable.setWidthPercentage(100);
        deptTable.setWidths(new float[]{30, 13, 13, 13, 14, 17});
        deptTable.setSpacingAfter(16);

        addPdfTableHeader(deptTable, new String[]{
                "Department Name", "Total", "On-Time", "Delayed", "On-Time %", "Performance Status"
        }, tableHeadFont, colorDarkHeader, colorBorder);

        List<DepartmentReportDTO> depts = new java.util.ArrayList<>(
                report.getDepartmentComparison() != null ? report.getDepartmentComparison() : java.util.Collections.emptyList()
        );
        depts.sort((a, b) -> Long.compare(b.getTotalPackets(), a.getTotalPackets()));

        long totalPacketsSum = 0;
        long onTimeSum = 0;
        long delayedSum = 0;
        boolean isZebra = false;

        for (DepartmentReportDTO d : depts) {
            totalPacketsSum += d.getTotalPackets();
            onTimeSum += d.getOnTime();
            delayedSum += d.getDelayed();

            BaseColor rowBg = isZebra ? colorSubtleBg : BaseColor.WHITE;
            isZebra = !isZebra;

            String statusTag;
            if (d.getOnTimeRate() >= 90.0) {
                statusTag = "Excellent (≥90%)";
            } else if (d.getOnTimeRate() >= 80.0) {
                statusTag = "Good (≥80%)";
            } else {
                statusTag = "Needs Attention (<80%)";
            }

            addPdfDataCell(deptTable, d.getDepartment(), Element.ALIGN_LEFT, rowBg, colorBorder, bodyFont, 5.5f);
            addPdfDataCell(deptTable, String.valueOf(d.getTotalPackets()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5.5f);
            addPdfDataCell(deptTable, String.valueOf(d.getOnTime()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5.5f);
            addPdfDataCell(deptTable, String.valueOf(d.getDelayed()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5.5f);
            addPdfDataCell(deptTable, d.getOnTimeRate() + "%", Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5.5f);
            addPdfDataCell(deptTable, statusTag, Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5.5f);
        }

        // Department Summary / Totals Row
        double weightedAvgRate = totalPacketsSum > 0
                ? Math.round((onTimeSum * 100.0 / totalPacketsSum) * 10.0) / 10.0 : 0.0;
        String overallStatus = weightedAvgRate >= 90.0 ? "Excellent (≥90%)"
                : (weightedAvgRate >= 80.0 ? "Good (≥80%)" : "Needs Attention (<80%)");

        addPdfDataCell(deptTable, "Total / Overall Average", Element.ALIGN_LEFT, colorSummaryBg, colorBorder, bodyBoldFont, 6f);
        addPdfDataCell(deptTable, String.valueOf(totalPacketsSum), Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 6f);
        addPdfDataCell(deptTable, String.valueOf(onTimeSum), Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 6f);
        addPdfDataCell(deptTable, String.valueOf(delayedSum), Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 6f);
        addPdfDataCell(deptTable, weightedAvgRate + "%", Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 6f);
        addPdfDataCell(deptTable, overallStatus, Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 6f);

        document.add(deptTable);

        // ── 5. Monthly Submission & Approval Trend (if available) ──
        if (report.getMonthlyTrend() != null && !report.getMonthlyTrend().isEmpty()) {
            Paragraph trendHeading = new Paragraph("3. MONTHLY SUBMISSION & APPROVAL TREND", sectionFont);
            trendHeading.setSpacingAfter(6);
            document.add(trendHeading);

            PdfPTable trendTable = new PdfPTable(4);
            trendTable.setWidthPercentage(100);
            trendTable.setWidths(new float[]{30, 23, 23, 24});
            trendTable.setSpacingAfter(16);

            addPdfTableHeader(trendTable, new String[]{
                    "Month / Period", "Submitted Packets", "Approved Packets", "Delayed Packets"
            }, tableHeadFont, colorDarkHeader, colorBorder);

            long mSubmittedSum = 0;
            long mApprovedSum = 0;
            long mDelayedSum = 0;
            boolean trendZebra = false;

            for (MonthlyTrendDTO m : report.getMonthlyTrend()) {
                mSubmittedSum += m.getSubmitted();
                mApprovedSum += m.getApproved();
                mDelayedSum += m.getDelayed();

                BaseColor rowBg = trendZebra ? colorSubtleBg : BaseColor.WHITE;
                trendZebra = !trendZebra;

                addPdfDataCell(trendTable, m.getMonth(), Element.ALIGN_LEFT, rowBg, colorBorder, bodyFont, 5f);
                addPdfDataCell(trendTable, String.valueOf(m.getSubmitted()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5f);
                addPdfDataCell(trendTable, String.valueOf(m.getApproved()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5f);
                addPdfDataCell(trendTable, String.valueOf(m.getDelayed()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5f);
            }

            // Trend Totals
            addPdfDataCell(trendTable, "Total", Element.ALIGN_LEFT, colorSummaryBg, colorBorder, bodyBoldFont, 5.5f);
            addPdfDataCell(trendTable, String.valueOf(mSubmittedSum), Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 5.5f);
            addPdfDataCell(trendTable, String.valueOf(mApprovedSum), Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 5.5f);
            addPdfDataCell(trendTable, String.valueOf(mDelayedSum), Element.ALIGN_CENTER, colorSummaryBg, colorBorder, bodyBoldFont, 5.5f);

            document.add(trendTable);
        }

        // ── 6. Primary Delay Bottlenecks / Reasons (if available) ──
        if (report.getDelayReasons() != null && !report.getDelayReasons().isEmpty()) {
            Paragraph delayHeading = new Paragraph("4. PRIMARY DELAY REASONS BREAKDOWN", sectionFont);
            delayHeading.setSpacingAfter(6);
            document.add(delayHeading);

            PdfPTable delayTable = new PdfPTable(3);
            delayTable.setWidthPercentage(100);
            delayTable.setWidths(new float[]{55, 20, 25});
            delayTable.setSpacingAfter(16);

            addPdfTableHeader(delayTable, new String[]{
                    "Delay Reason Category", "Affected Packets", "Percentage Share"
            }, tableHeadFont, colorDarkHeader, colorBorder);

            boolean delayZebra = false;
            for (DelayReasonDTO dr : report.getDelayReasons()) {
                BaseColor rowBg = delayZebra ? colorSubtleBg : BaseColor.WHITE;
                delayZebra = !delayZebra;

                addPdfDataCell(delayTable, dr.getReason(), Element.ALIGN_LEFT, rowBg, colorBorder, bodyFont, 5f);
                addPdfDataCell(delayTable, String.valueOf(dr.getCount()), Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5f);
                addPdfDataCell(delayTable, dr.getPercentage() + "%", Element.ALIGN_CENTER, rowBg, colorBorder, bodyFont, 5f);
            }

            document.add(delayTable);
        }

        document.close();
    }

    // ── PDF HELPER METHODS ────────────────────────────────────

    private static class PdfFooterPageEvent extends PdfPageEventHelper {
        private final com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                com.itextpdf.text.Font.NORMAL, new BaseColor(110, 115, 125));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            try {
                footer.setWidths(new float[]{70, 30});
                footer.setTotalWidth(document.right() - document.left());
                footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                PdfPCell leftCell = new PdfPCell(new Phrase("Confidential - For Internal Administrative Use Only", footerFont));
                leftCell.setBorder(Rectangle.TOP);
                leftCell.setBorderColor(new BaseColor(210, 215, 222));
                leftCell.setBorderWidth(0.5f);
                leftCell.setPaddingTop(5);
                leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);

                PdfPCell rightCell = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
                rightCell.setBorder(Rectangle.TOP);
                rightCell.setBorderColor(new BaseColor(210, 215, 222));
                rightCell.setBorderWidth(0.5f);
                rightCell.setPaddingTop(5);
                rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                footer.addCell(leftCell);
                footer.addCell(rightCell);

                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
            } catch (Exception ignored) {
            }
        }
    }

    private PdfPCell createMetaCell(String label, String value,
                                    com.itextpdf.text.Font labelFont,
                                    com.itextpdf.text.Font valFont,
                                    BaseColor bg, BaseColor border) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value != null ? value : "-", valFont));

        PdfPCell cell = new PdfPCell(p);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setBorderWidth(0.5f);
        cell.setPadding(6f);
        return cell;
    }

    private void addPdfTableHeader(PdfPTable table, String[] headers,
                                   com.itextpdf.text.Font font,
                                   BaseColor bg, BaseColor border) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(bg);
            cell.setBorderColor(border);
            cell.setBorderWidth(0.5f);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private void addKpiValueCell(PdfPTable table, String value,
                                com.itextpdf.text.Font font,
                                BaseColor bg, BaseColor border) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setBorderWidth(0.5f);
        cell.setPadding(9f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addPdfDataCell(PdfPTable table, String value, int align,
                               BaseColor bg, BaseColor border,
                               com.itextpdf.text.Font font, float padding) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setBorderWidth(0.5f);
        cell.setPadding(padding);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}