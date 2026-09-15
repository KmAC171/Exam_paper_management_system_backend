package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LecturerTaskService {

    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final PacketRepository packetRepository;
    private final MarkingRepository markingRepository;
    private final PrintingScheduleRepository printingScheduleRepository;
    private final PacketStatusRepository packetStatusRepository;
    private final UserRepository userRepository;

    private String cleanCycleId(String cycleId) {
        if (cycleId == null) return null;
        String trimmed = cycleId.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "undefined".equalsIgnoreCase(trimmed)) {
            return null;
        }
        if (trimmed.contains(",")) {
            trimmed = trimmed.split(",")[0].trim();
        }
        return trimmed;
    }

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public Set<ExamPacket> getPacketsForLecturer(String lecturerId) {
        return getPacketsForLecturer(lecturerId, null);
    }

    public Set<ExamPacket> getPacketsForLecturer(String lecturerId, String cycleId) {
        Long lId = null;
        if (lecturerId != null && !lecturerId.isBlank()) {
            User user = userRepository.findByUsername(lecturerId).orElse(null);
            if (user != null) {
                lId = user.getUserId();
            } else {
                lId = parseId(lecturerId);
            }
        }

        Set<ExamPacket> packets = new HashSet<>();
        if (lId != null) {
            String cleanedCycleId = cleanCycleId(cycleId);
            boolean isAllCycles = "ALL".equalsIgnoreCase(cleanedCycleId);

            if (cleanedCycleId != null && !isAllCycles) {
                packets.addAll(packetRepository.findByLecturerOrModeratorIdAndCycleId(lId, cleanedCycleId));
            } else {
                packets.addAll(packetRepository.findByLecturerOrModeratorId(lId));
            }

            List<PacketAssignment> assignments = packetAssignmentRepository.findByUserUserId(lId);
            for (PacketAssignment a : assignments) {
                if (a.getPacket() != null) {
                    if (cleanedCycleId == null || isAllCycles || (a.getPacket().getAcademicCycle() != null && cleanedCycleId.equalsIgnoreCase(a.getPacket().getAcademicCycle().getCycleId()))) {
                        packets.add(a.getPacket());
                    }
                }
            }
        }
        return packets;
    }

    public LecturerTaskSummaryResponseDTO getTaskSummary(String lecturerId) {
        Set<ExamPacket> packets = getPacketsForLecturer(lecturerId);
        long pending = 0;
        long completed = 0;
        long overdue = 0;
        LocalDate today = LocalDate.now();

        for (ExamPacket packet : packets) {
            String status = packet.getStatus() != null ? packet.getStatus().getStatusName() : "";
            LocalDate deadline = packet.getDeadline();

            if ("COMPLETED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                completed++;
            } else if (deadline != null && deadline.isBefore(today)) {
                overdue++;
            } else {
                pending++;
            }
        }

        return new LecturerTaskSummaryResponseDTO(lecturerId, pending, completed, overdue);
    }

    public LecturerWorkloadStatisticsDTO getWorkloadStatistics(String lecturerId) {
        Set<ExamPacket> packets = getPacketsForLecturer(lecturerId);
        long totalAssignedPackets = packets.size();
        long completedPackets = 0;
        long pendingPackets = 0;
        long overduePackets = 0;
        LocalDate today = LocalDate.now();

        for (ExamPacket packet : packets) {
            String status = packet.getStatus() != null ? packet.getStatus().getStatusName() : "";
            LocalDate deadline = packet.getDeadline();

            if ("COMPLETED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                completedPackets++;
            } else if (deadline != null && deadline.isBefore(today)) {
                overduePackets++;
            } else {
                pendingPackets++;
            }
        }

        Long lId = parseId(lecturerId);
        if (lId == null) {
            User user = userRepository.findByUsername(lecturerId).orElse(null);
            if (user != null) lId = user.getUserId();
        }

        int totalScripts = 0;
        int markedScripts = 0;
        if (lId != null) {
            List<Marking> markings = markingRepository.findByLecturerUserId(lId);
            for (Marking m : markings) {
                if (m.getTotalScripts() != null) totalScripts += m.getTotalScripts();
                if (m.getMarkedScripts() != null) markedScripts += m.getMarkedScripts();
            }
        }

        int remainingScripts = Math.max(0, totalScripts - markedScripts);

        return new LecturerWorkloadStatisticsDTO(
                lecturerId,
                totalAssignedPackets,
                totalScripts,
                markedScripts,
                remainingScripts,
                completedPackets,
                pendingPackets,
                overduePackets
        );
    }

    public List<LecturerDeadlineCalendarDTO> getDeadlineCalendar(String lecturerId) {
        return getDeadlineCalendar(lecturerId, null);
    }

    public List<LecturerDeadlineCalendarDTO> getDeadlineCalendar(String lecturerId, String cycleId) {
        Set<ExamPacket> packets = getPacketsForLecturer(lecturerId, cycleId);
        List<LecturerDeadlineCalendarDTO> deadlines = new ArrayList<>();

        for (ExamPacket packet : packets) {
            if (packet.getCourse() == null) continue;

            LocalDate deadline = packet.getDeadline();
            if (deadline == null && packet.getModerationDeadline() != null) {
                deadline = packet.getModerationDeadline();
            }
            if (deadline == null) continue;

            String status = packet.getStatus() != null ? packet.getStatus().getStatusName() : "PENDING";

            // Exclude already finalized packets from upcoming deadlines
            if ("COMPLETED".equalsIgnoreCase(status) || "MARKING COMPLETE".equalsIgnoreCase(status) || "MARKING_COMPLETE".equalsIgnoreCase(status)) {
                continue;
            }

            String packetIdStr = String.format("PKT-%d-%03d",
                    packet.getDeadline() != null ? packet.getDeadline().getYear() : 2026,
                    packet.getPacketId());

            deadlines.add(new LecturerDeadlineCalendarDTO(
                    packetIdStr,
                    packet.getCourse().getCourseCode(),
                    packet.getCourse().getCourseName(),
                    deadline,
                    status
            ));
        }

        deadlines.sort(Comparator.comparing(LecturerDeadlineCalendarDTO::getDeadline));
        return deadlines;
    }

    public List<LecturerPrintingScheduleDTO> getPrintingSchedules(String lecturerId) {
        Set<ExamPacket> packets = getPacketsForLecturer(lecturerId);
        List<LecturerPrintingScheduleDTO> response = new ArrayList<>();

        for (ExamPacket packet : packets) {
            if (packet.getCourse() == null) continue;
            List<PrintingSchedule> schedules = printingScheduleRepository.findByPacketPacketId(packet.getPacketId());
            if (schedules == null || schedules.isEmpty()) {
                response.add(new LecturerPrintingScheduleDTO(
                        String.valueOf(packet.getPacketId()),
                        packet.getCourse().getCourseCode(),
                        packet.getCourse().getCourseName(),
                        "Scheduled",
                        packet.getExamDate() != null ? packet.getExamDate() : packet.getDeadline()
                ));
            } else {
                for (PrintingSchedule s : schedules) {
                    response.add(new LecturerPrintingScheduleDTO(
                            String.valueOf(packet.getPacketId()),
                            packet.getCourse().getCourseCode(),
                            packet.getCourse().getCourseName(),
                            s.getStatus() != null ? s.getStatus() : "Scheduled",
                            packet.getExamDate() != null ? packet.getExamDate() : packet.getDeadline()
                    ));
                }
            }
        }
        return response;
    }

    @Transactional
    public String updatePacketStatus(String packetId, UpdatePacketStatusRequestDTO request) {
        Long pId = parseId(packetId);
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found: " + packetId));

        if (request != null && request.getStatus() != null) {
            PacketStatus status = packetStatusRepository.findByStatusName(request.getStatus().toUpperCase())
                    .orElse(null);
            if (status != null) {
                packet.setStatus(status);
                examPacketRepository.save(packet);
            }
        }
        return "Packet status updated successfully";
    }

    @Transactional
    public CompleteTaskResponseDTO completeTask(String packetId) {
        Long pId = parseId(packetId);
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found: " + packetId));

        PacketStatus completedStatus = packetStatusRepository.findByStatusName("COMPLETED")
                .orElse(null);
        if (completedStatus != null) {
            packet.setStatus(completedStatus);
            examPacketRepository.save(packet);
        }

        return CompleteTaskResponseDTO.builder()
                .packetId(packetId)
                .status("COMPLETED")
                .message("Task marked as completed")
                .build();
    }
}
