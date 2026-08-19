package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.ActivityLog;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.PacketRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final PacketRepository packetRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    private static final List<String> STAGE_ORDER = List.of(
            "DRAFT", "PENDING", "UNDER_MODERATION",
            "APPROVED", "PRINTING_QUEUE", "COMPLETED"
    );

    private static final Map<String, String> STAGE_LABELS = Map.of(
            "DRAFT", "Draft",
            "PENDING", "Submitted",
            "UNDER_MODERATION", "Moderation",
            "APPROVED", "Approved",
            "PRINTING_QUEUE", "Printing",
            "COMPLETED", "Completed"
    );

    private static final Map<String, String> STAGE_ACTORS = Map.of(
            "DRAFT", "Lecturer",
            "PENDING", "Lecturer → System",
            "UNDER_MODERATION", "Moderator",
            "APPROVED", "HOD + Moderator",
            "PRINTING_QUEUE", "Registry",
            "COMPLETED", "System"
    );

    public List<WorkflowPacketDTO> getWorkflowPackets(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExamPacket> packets;
        switch (role) {
            case "ROLE_ADMIN", "ROLE_GUEST" ->
                    packets = packetRepository.findAllWithDetails();
            case "ROLE_USER" ->
                    packets = packetRepository.findByLecturerId(user.getUserId());
            case "ROLE_MODERATOR" ->
                    packets = packetRepository.findByModeratorId(user.getUserId());
            default -> packets = List.of();
        }

        return packets.stream()
                .map(this::toWorkflowDTO)
                .collect(Collectors.toList());
    }

    private WorkflowPacketDTO toWorkflowDTO(ExamPacket p) {
        String currentStatus = p.getStatus().getStatusName();
        int currentStageIndex = STAGE_ORDER.indexOf(currentStatus);
        int currentStage = currentStageIndex + 1;

        String packetIdStr = String.format("PKT-%d-%03d",
                p.getDeadline() != null ? p.getDeadline().getYear() : 2026,
                p.getPacketId());

        // get history for this packet
        List<ActivityLog> history = activityLogService.getPacketHistory(p.getPacketId());

        // group history events by stageName
        Map<String, List<ActivityLog>> byStage = history.stream()
                .filter(h -> h.getStageName() != null)
                .collect(Collectors.groupingBy(ActivityLog::getStageName));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");

        List<WorkflowStageDTO> stages = new ArrayList<>();
        for (int i = 0; i < STAGE_ORDER.size(); i++) {
            String stageKey = STAGE_ORDER.get(i);
            boolean completed = i < currentStageIndex;
            boolean current = i == currentStageIndex;

            List<WorkflowEventDTO> events = byStage
                    .getOrDefault(stageKey, List.of())
                    .stream()
                    .map(log -> new WorkflowEventDTO(
                            log.getMessage(),
                            log.getActorName(),
                            log.getCreatedAt() != null
                                    ? log.getCreatedAt().format(fmt) + " · " + log.getActorName()
                                    : ""
                    ))
                    .collect(Collectors.toList());

            stages.add(new WorkflowStageDTO(
                    STAGE_LABELS.get(stageKey),
                    STAGE_ACTORS.get(stageKey),
                    completed,
                    current,
                    events
            ));
        }

        return new WorkflowPacketDTO(
                packetIdStr,
                p.getCourse().getCourseCode(),
                p.getCourse().getCourseName(),
                currentStatus,
                currentStage,
                6,
                stages
        );
    }
}