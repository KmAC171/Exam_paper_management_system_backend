package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.AcademicCycleDTO;
import com.exam_paper.backend.entity.AcademicCycle;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.AcademicCycleRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicCycleService {

    private final AcademicCycleRepository academicCycleRepository;
    private final ExamPacketRepository examPacketRepository;
    private final PacketRepository packetRepository;

    @PostConstruct
    @Transactional
    public void initDefaultCyclesAndBackfill() {
        try {
            // Ensure at least 1 active cycle exists
            Optional<AcademicCycle> activeOpt = academicCycleRepository.findFirstByStatusOrderByStartDateDesc("ACTIVE");
            AcademicCycle activeCycle;

            if (activeOpt.isEmpty()) {
                // If there are existing cycles, activate the latest one, else create default
                List<AcademicCycle> allCycles = academicCycleRepository.findAllByOrderByStartDateDesc();
                if (!allCycles.isEmpty()) {
                    activeCycle = allCycles.get(0);
                    activeCycle.setStatus("ACTIVE");
                    academicCycleRepository.save(activeCycle);
                } else {
                    activeCycle = AcademicCycle.builder()
                            .cycleId("2025-2026-SEM1")
                            .academicYear("2025/2026")
                            .year(2025)
                            .semester(1)
                            .cycleName("2025/2026 - Semester 1")
                            .startDate(LocalDate.of(2025, 9, 1))
                            .endDate(LocalDate.of(2026, 1, 31))
                            .status("ACTIVE")
                            .build();
                    academicCycleRepository.save(activeCycle);

                    // Add a planned second semester
                    AcademicCycle sem2 = AcademicCycle.builder()
                            .cycleId("2025-2026-SEM2")
                            .academicYear("2025/2026")
                            .year(2026)
                            .semester(2)
                            .cycleName("2025/2026 - Semester 2")
                            .startDate(LocalDate.of(2026, 2, 1))
                            .endDate(LocalDate.of(2026, 6, 30))
                            .status("PLANNED")
                            .build();
                    academicCycleRepository.save(sem2);
                }
            } else {
                activeCycle = activeOpt.get();
            }

            // Backfill any packets without cycle_id
            List<ExamPacket> unassignedPackets = examPacketRepository.findPacketsWithoutCycle();
            if (!unassignedPackets.isEmpty()) {
                log.info("Backfilling {} exam packets to active cycle {}", unassignedPackets.size(), activeCycle.getCycleId());
                for (ExamPacket p : unassignedPackets) {
                    p.setAcademicCycle(activeCycle);
                }
                packetRepository.saveAll(unassignedPackets);
            }
        } catch (Exception e) {
            log.error("Failed to initialize default academic cycles: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AcademicCycleDTO> getAllCycles() {
        return academicCycleRepository.findAllByOrderByStartDateDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicCycleDTO getActiveCycle() {
        AcademicCycle cycle = academicCycleRepository.findFirstByStatusOrderByStartDateDesc("ACTIVE")
                .or(() -> academicCycleRepository.findAllByOrderByStartDateDesc().stream().findFirst())
                .orElseGet(() -> {
                    AcademicCycle fallback = AcademicCycle.builder()
                            .cycleId("2025-2026-SEM1")
                            .academicYear("2025/2026")
                            .year(2025)
                            .semester(1)
                            .cycleName("2025/2026 - Semester 1")
                            .startDate(LocalDate.now().minusMonths(2))
                            .endDate(LocalDate.now().plusMonths(3))
                            .status("ACTIVE")
                            .build();
                    return academicCycleRepository.save(fallback);
                });
        return toDTO(cycle);
    }

    @Transactional(readOnly = true)
    public AcademicCycle getActiveCycleEntity() {
        return academicCycleRepository.findFirstByStatusOrderByStartDateDesc("ACTIVE")
                .or(() -> academicCycleRepository.findAllByOrderByStartDateDesc().stream().findFirst())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public AcademicCycleDTO getCycleById(String cycleId) {
        AcademicCycle cycle = academicCycleRepository.findByCycleId(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Academic Cycle not found: " + cycleId));
        return toDTO(cycle);
    }

    @Transactional
    public AcademicCycleDTO createCycle(AcademicCycleDTO dto) {
        if (dto.getAcademicYear() == null || dto.getAcademicYear().trim().isEmpty()) {
            throw new IllegalArgumentException("Academic year is required (e.g. 2026/2027)");
        }
        if (dto.getSemester() == null || (dto.getSemester() != 1 && dto.getSemester() != 2)) {
            throw new IllegalArgumentException("Semester must be 1 or 2");
        }

        String yearClean = dto.getAcademicYear().replaceAll("[^0-9]", "-");
        String cycleId = dto.getCycleId() != null && !dto.getCycleId().trim().isEmpty()
                ? dto.getCycleId().trim()
                : "AY" + yearClean + "-SEM" + dto.getSemester();

        if (academicCycleRepository.existsByCycleId(cycleId)) {
            throw new IllegalArgumentException("Academic Cycle with ID '" + cycleId + "' already exists.");
        }

        String cycleName = dto.getCycleName() != null && !dto.getCycleName().trim().isEmpty()
                ? dto.getCycleName().trim()
                : dto.getAcademicYear() + " - Semester " + dto.getSemester();

        LocalDate start = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now();
        LocalDate end = dto.getEndDate() != null ? dto.getEndDate() : start.plusMonths(5);

        String status = dto.getStatus() != null ? dto.getStatus().toUpperCase() : "PLANNED";

        if ("ACTIVE".equalsIgnoreCase(status)) {
            // Archive or set completed any currently active cycle
            List<AcademicCycle> currentActives = academicCycleRepository.findByStatus("ACTIVE");
            for (AcademicCycle c : currentActives) {
                c.setStatus("COMPLETED");
                academicCycleRepository.save(c);
            }
        }

        AcademicCycle cycle = AcademicCycle.builder()
                .cycleId(cycleId)
                .academicYear(dto.getAcademicYear().trim())
                .year(start.getYear())
                .semester(dto.getSemester())
                .cycleName(cycleName)
                .startDate(start)
                .endDate(end)
                .status(status)
                .build();

        AcademicCycle saved = academicCycleRepository.save(cycle);
        return toDTO(saved);
    }

    @Transactional
    public AcademicCycleDTO activateCycle(String cycleId) {
        AcademicCycle target = academicCycleRepository.findByCycleId(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Academic cycle not found: " + cycleId));

        // Archive currently active cycles
        List<AcademicCycle> actives = academicCycleRepository.findByStatus("ACTIVE");
        for (AcademicCycle c : actives) {
            if (!c.getCycleId().equals(cycleId)) {
                c.setStatus("COMPLETED");
                academicCycleRepository.save(c);
            }
        }

        target.setStatus("ACTIVE");
        AcademicCycle saved = academicCycleRepository.save(target);
        return toDTO(saved);
    }

    @Transactional
    public AcademicCycleDTO updateCycleStatus(String cycleId, String newStatus) {
        AcademicCycle cycle = academicCycleRepository.findByCycleId(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Academic cycle not found: " + cycleId));

        if ("ACTIVE".equalsIgnoreCase(newStatus)) {
            return activateCycle(cycleId);
        }

        cycle.setStatus(newStatus.toUpperCase());
        AcademicCycle saved = academicCycleRepository.save(cycle);
        return toDTO(saved);
    }

    private AcademicCycleDTO toDTO(AcademicCycle cycle) {
        long total = packetRepository.countByAcademicCycle_CycleId(cycle.getCycleId());
        long completed = examPacketRepository.countCompletedByCycle(cycle.getCycleId());
        long delayed = examPacketRepository.countDelayedByCycle(cycle.getCycleId());
        double rate = total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return AcademicCycleDTO.builder()
                .cycleId(cycle.getCycleId())
                .academicYear(cycle.getAcademicYear())
                .year(cycle.getYear())
                .semester(cycle.getSemester())
                .cycleName(cycle.getCycleName())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .status(cycle.getStatus())
                .totalPackets(total)
                .completedPackets(completed)
                .delayedPackets(delayed)
                .completionRate(rate)
                .build();
    }
}
