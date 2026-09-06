package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.LecturerDashboardResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LecturerDashboardService {

    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final MarkingRepository markingRepository;
    private final UserRepository userRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public LecturerDashboardResponseDTO getDashboard(String lecturerId) {
        Long lId = parseId(lecturerId);
        if (lId == null) {
            User user = userRepository.findByUsername(lecturerId).orElse(null);
            if (user != null) {
                lId = user.getUserId();
            }
        }

        Set<ExamPacket> packetSet = new HashSet<>();
        if (lId != null) {
            List<PacketAssignment> assignments = packetAssignmentRepository.findByUserUserId(lId);
            for (PacketAssignment a : assignments) {
                if (a.getPacket() != null) {
                    packetSet.add(a.getPacket());
                }
            }
            packetSet.addAll(examPacketRepository.findByLecturerUserId(lId));
        }

        long totalActiveTasks = 0;
        long completedTasks = 0;
        long overdueItems = 0;
        int totalScripts = 0;

        LocalDate today = LocalDate.now();
        LocalDate nextDeadline = null;

        for (ExamPacket packet : packetSet) {
            totalActiveTasks++;

            Optional<Marking> markingOpt = markingRepository.findByPacketPacketId(packet.getPacketId());
            if (markingOpt.isPresent() && markingOpt.get().getTotalScripts() != null) {
                totalScripts += markingOpt.get().getTotalScripts();
            }

            String statusName = packet.getStatus() != null ? packet.getStatus().getStatusName() : "";

            if ("COMPLETED".equalsIgnoreCase(statusName) || "APPROVED".equalsIgnoreCase(statusName)) {
                completedTasks++;
            }

            if (packet.getDeadline() != null &&
                    packet.getDeadline().isBefore(today) &&
                    !"COMPLETED".equalsIgnoreCase(statusName) &&
                    !"APPROVED".equalsIgnoreCase(statusName)) {
                overdueItems++;
            }

            if (packet.getDeadline() != null &&
                    !packet.getDeadline().isBefore(today) &&
                    !"COMPLETED".equalsIgnoreCase(statusName) &&
                    !"APPROVED".equalsIgnoreCase(statusName)) {
                if (nextDeadline == null || packet.getDeadline().isBefore(nextDeadline)) {
                    nextDeadline = packet.getDeadline();
                }
            }
        }

        return new LecturerDashboardResponseDTO(
                lecturerId,
                totalActiveTasks,
                completedTasks,
                overdueItems,
                totalScripts,
                nextDeadline
        );
    }
}
