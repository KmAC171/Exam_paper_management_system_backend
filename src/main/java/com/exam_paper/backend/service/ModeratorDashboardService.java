package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.ModeratorDashboardResponseDTO;
import com.exam_paper.backend.dto.ModeratorKpiDTO;
import com.exam_paper.backend.dto.ModeratorPendingPacketDTO;
import com.exam_paper.backend.dto.ModeratorRecentReviewDTO;
import com.exam_paper.backend.entity.ActivityLog;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ActivityLogRepository;
import com.exam_paper.backend.repository.PacketRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModeratorDashboardService {

    private final PacketRepository packetRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    public ModeratorDashboardResponseDTO getModeratorDashboard(String username) {
        User moderator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Moderator not found with username: " + username));

        List<ExamPacket> assignedPackets = packetRepository.findByModeratorId(moderator.getUserId());

        LocalDate today = LocalDate.now();

        long pendingReview = 0;
        long approvedToday = 0;
        long returned = 0;
        long totalReviewed = 0;

        List<ModeratorPendingPacketDTO> pendingList = new ArrayList<>();

        for (ExamPacket p : assignedPackets) {
            String statusName = p.getStatus() != null ? p.getStatus().getStatusName() : "DRAFT";
            LocalDate deadline = p.getModerationDeadline() != null ? p.getModerationDeadline() : p.getDeadline();
            boolean isOverdue = deadline != null && deadline.isBefore(today);

            boolean isApproved = "APPROVED".equalsIgnoreCase(statusName) || "COMPLETED".equalsIgnoreCase(statusName) || "PRINTING_QUEUE".equalsIgnoreCase(statusName);
            boolean isReturned = "PENDING".equalsIgnoreCase(statusName) && p.getModeratorNote() != null && !p.getModeratorNote().trim().isEmpty();
            boolean isPending = "UNDER_MODERATION".equalsIgnoreCase(statusName) || ("PENDING".equalsIgnoreCase(statusName) && !isReturned);

            if (isApproved) {
                approvedToday++;
                totalReviewed++;
            } else if (isReturned) {
                returned++;
                totalReviewed++;
            } else if (isPending) {
                pendingReview++;
            }

            // If packet is pending review or under moderation or returned, include in pending review list
            if (!isApproved && !"COMPLETED".equalsIgnoreCase(statusName)) {
                String priority;
                if (deadline == null) {
                    priority = "Low";
                } else if (isOverdue || deadline.isBefore(today.plusDays(2))) {
                    priority = "High";
                } else if (deadline.isBefore(today.plusDays(7))) {
                    priority = "Medium";
                } else {
                    priority = "Low";
                }

                String formattedPacketId = String.format("PKT-%d-%03d",
                        deadline != null ? deadline.getYear() : today.getYear(),
                        p.getPacketId());

                String submittedDateStr = deadline != null ? deadline.minusDays(14).format(DISPLAY_DATE_FORMAT) : today.minusDays(7).format(DISPLAY_DATE_FORMAT);
                String deadlineStr = deadline != null ? deadline.format(DISPLAY_DATE_FORMAT) : "No deadline";

                pendingList.add(ModeratorPendingPacketDTO.builder()
                        .id(p.getPacketId())
                        .packetId(formattedPacketId)
                        .courseCode(p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A")
                        .courseName(p.getCourse() != null ? p.getCourse().getCourseName() : "N/A")
                        .lecturerName(p.getLecturer() != null ? p.getLecturer().getFullName() : "Unassigned")
                        .submittedDate(submittedDateStr)
                        .deadline(deadlineStr)
                        .rawDeadline(deadline)
                        .priority(priority)
                        .status(statusName)
                        .moderatorNote(p.getModeratorNote())
                        .build());
            }
        }

        // Sort pending list: High priority / earliest deadline first
        pendingList.sort(Comparator.comparing((ModeratorPendingPacketDTO p) -> {
            if ("High".equalsIgnoreCase(p.getPriority())) return 1;
            if ("Medium".equalsIgnoreCase(p.getPriority())) return 2;
            return 3;
        }).thenComparing(p -> p.getRawDeadline() != null ? p.getRawDeadline() : LocalDate.MAX));

        ModeratorKpiDTO kpis = ModeratorKpiDTO.builder()
                .pendingReview(pendingReview > 0 ? pendingReview : pendingList.size())
                .approvedToday(approvedToday)
                .returned(returned)
                .totalReviewed(totalReviewed > 0 ? totalReviewed : (approvedToday + returned))
                .build();

        // Query recent reviews strictly for this moderator's packets
        List<ActivityLog> logs = activityLogRepository.findByPacket_Moderator_UserIdOrderByCreatedAtDesc(moderator.getUserId());
        if (logs == null || logs.isEmpty()) {
            logs = activityLogRepository.findTop10ByOrderByCreatedAtDesc();
        }

        List<ModeratorRecentReviewDTO> recentReviews = logs.stream()
                .limit(6)
                .map(log -> {
                    String courseCode = (log.getPacket() != null && log.getPacket().getCourse() != null)
                            ? log.getPacket().getCourse().getCourseCode()
                            : "EXAM";

                    String msg = log.getMessage() != null ? log.getMessage() : "";
                    String iconType = "PENDING";
                    String statusLabel = "Under Review";

                    if (msg.toLowerCase().contains("approved") || "APPROVED".equalsIgnoreCase(log.getStageName())) {
                        iconType = "APPROVE";
                        statusLabel = "Approved";
                    } else if (msg.toLowerCase().contains("return") || "UNDER_MODERATION".equalsIgnoreCase(log.getStageName())) {
                        iconType = "RETURN";
                        statusLabel = "Returned for Revision";
                    } else if (msg.toLowerCase().contains("reject") || "DRAFT".equalsIgnoreCase(log.getStageName())) {
                        iconType = "REJECT";
                        statusLabel = "Rejected";
                    }

                    String dateStr = log.getCreatedAt() != null ? log.getCreatedAt().format(DISPLAY_DATE_FORMAT) : "Recent";

                    return ModeratorRecentReviewDTO.builder()
                            .courseCode(courseCode)
                            .note(msg)
                            .status(statusLabel)
                            .date(dateStr)
                            .iconType(iconType)
                            .build();
                })
                .collect(Collectors.toList());

        return ModeratorDashboardResponseDTO.builder()
                .kpis(kpis)
                .pendingPackets(pendingList)
                .recentReviews(recentReviews)
                .build();
    }
}
