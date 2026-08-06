package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.repository.DelayReasonRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public ReportResponseDTO getReport() {
        LocalDate today = LocalDate.now();

        // KPI calculations
        long total = examPacketRepository.count();
        long completed = examPacketRepository.countByStatus_StatusName("COMPLETED")
                + examPacketRepository.countByStatus_StatusName("APPROVED");
        long delayed = examPacketRepository.countDelayed(today);
        long onTime = total - delayed;

        double completionRate = total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0;
        double onTimeRate = total > 0 ? Math.round((onTime * 100.0 / total) * 10.0) / 10.0 : 0;
        double avgProcessingDays = 4.8; // placeholder — needs created_at tracking to calculate precisely

        ReportKpiDTO kpi = new ReportKpiDTO(completionRate, avgProcessingDays, onTimeRate, delayed);

        // Monthly trend
        List<MonthlyTrendDTO> monthlyTrend = examPacketRepository.getMonthlyTrend()
                .stream()
                .map(p -> new MonthlyTrendDTO(
                        MONTH_NAMES[p.getMonth()],
                        p.getSubmitted(),
                        p.getApproved(),
                        p.getDelayed()
                ))
                .collect(Collectors.toList());

        // Delay reasons
        List<Object[]> reasonCounts = delayReasonRepository.countByReason();
        long totalReasons = reasonCounts.stream().mapToLong(r -> (Long) r[1]).sum();
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
        List<DepartmentReportDTO> departments = examPacketRepository.getDepartmentReport()
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
                            deptOnTimeRate
                    );
                })
                .collect(Collectors.toList());

        return new ReportResponseDTO(kpi, monthlyTrend, delayReasons, departments);
    }
}