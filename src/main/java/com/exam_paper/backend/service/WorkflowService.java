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

    public record StageDefinition(String stageKey, String label, String actor, List<String> matchingStatuses) {}

    public static final List<StageDefinition> WORKFLOW_STAGES = List.of(
            new StageDefinition("PENDING", "Pending", "Registry assigned",
                    List.of("PENDING", "INITIALIZED", "CREATED")),
            new StageDefinition("DRAFT", "Drafting", "Lecturer preparing",
                    List.of("DRAFT", "DRAFTING", "START_DRAFT")),
            new StageDefinition("MODERATION", "Moderation", "Moderator review",
                    List.of("SUBMITTED", "UNDER_MODERATION", "MODERATION", "REJECTED", "RETURNED", "DELAYED")),
            new StageDefinition("APPROVED", "Approved", "Moderator approved",
                    List.of("APPROVED", "APPROVE")),
            new StageDefinition("PRINTING", "Printing", "Lecturer printing",
                    List.of("PRINTING", "PRINTING_QUEUE", "PRINT")),
            new StageDefinition("PAPERS_STORED", "Papers Stored", "Safe custody",
                    List.of("PAPERS STORED", "PAPERS_STORED", "STORED", "SAFE_CUSTODY")),
            new StageDefinition("ANSWER_SHEETS_TAKEN", "Answer Sheets Taken", "Exam finished & collected",
                    List.of("ANSWER SHEETS TAKEN", "ANSWER_SHEETS_TAKEN", "SHEETS_TAKEN")),
            new StageDefinition("MARKING", "Marking", "Lecturer marking",
                    List.of("MARKING", "UNDER_MARKING")),
            new StageDefinition("MARKING_COMPLETE", "Marking Complete", "Finalized",
                    List.of("MARKING COMPLETE", "MARKING_COMPLETE", "COMPLETED", "COMPLETE", "FINALIZED"))
    );

    public static int getStageIndexForStatus(String status) {
        if (status == null || status.isBlank()) return 0;
        String normalized = status.trim().toUpperCase().replace("-", "_");
        for (int i = 0; i < WORKFLOW_STAGES.size(); i++) {
            StageDefinition def = WORKFLOW_STAGES.get(i);
            for (String match : def.matchingStatuses()) {
                if (match.equalsIgnoreCase(normalized)
                        || match.replace(" ", "_").equalsIgnoreCase(normalized)
                        || match.replace("_", " ").equalsIgnoreCase(normalized)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public List<WorkflowPacketDTO> getWorkflowPackets(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String userRole = user.getRole() != null ? user.getRole().name() : (role != null ? role : "ROLE_ADMIN");

        List<ExamPacket> packets;
        switch (userRole) {
            case "ROLE_ADMIN", "ROLE_GUEST", "ROLE_SYSTEM_ADMIN" ->
                    packets = packetRepository.findAllWithDetails();
            case "ROLE_USER" ->
                    packets = packetRepository.findByLecturerId(user.getUserId());
            case "ROLE_MODERATOR" ->
                    packets = packetRepository.findByModeratorId(user.getUserId());
            default ->
                    packets = List.of();
        }

        return packets.stream()
                .map(this::toWorkflowDTO)
                .collect(Collectors.toList());
    }

    private WorkflowPacketDTO toWorkflowDTO(ExamPacket p) {
        String currentStatus = p.getStatus() != null ? p.getStatus().getStatusName() : "PENDING";
        int currentStageIndex = getStageIndexForStatus(currentStatus);

        boolean isPacketCompleted = "COMPLETED".equalsIgnoreCase(currentStatus)
                || "MARKING COMPLETE".equalsIgnoreCase(currentStatus)
                || "MARKING_COMPLETE".equalsIgnoreCase(currentStatus)
                || "COMPLETE".equalsIgnoreCase(currentStatus)
                || "FINALIZED".equalsIgnoreCase(currentStatus);

        int totalStages = WORKFLOW_STAGES.size();
        int currentStage = isPacketCompleted ? totalStages : (currentStageIndex + 1);

        String packetIdStr = String.format("PKT-%d-%03d",
                p.getDeadline() != null ? p.getDeadline().getYear() : 2026,
                p.getPacketId());

        // get history for this packet
        List<ActivityLog> history = activityLogService.getPacketHistory(p.getPacketId());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");

        // Map events to stage indices
        Map<Integer, List<WorkflowEventDTO>> eventsByStageIndex = new HashMap<>();
        for (ActivityLog log : history) {
            int logStageIdx = getStageIndexForStatus(log.getStageName());
            eventsByStageIndex.computeIfAbsent(logStageIdx, k -> new ArrayList<>())
                    .add(new WorkflowEventDTO(
                            log.getMessage(),
                            log.getActorName(),
                            log.getCreatedAt() != null
                                    ? log.getCreatedAt().format(fmt) + (log.getActorName() != null && !log.getActorName().isBlank() ? " · " + log.getActorName() : "")
                                    : ""
                    ));
        }

        List<WorkflowStageDTO> stages = new ArrayList<>();
        for (int i = 0; i < totalStages; i++) {
            StageDefinition def = WORKFLOW_STAGES.get(i);
            boolean completed = isPacketCompleted || (i < currentStageIndex);
            boolean current = !isPacketCompleted && (i == currentStageIndex);

            List<WorkflowEventDTO> events = eventsByStageIndex.getOrDefault(i, List.of());

            stages.add(new WorkflowStageDTO(
                    def.label(),
                    def.actor(),
                    completed,
                    current,
                    events
            ));
        }

        return new WorkflowPacketDTO(
                packetIdStr,
                p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A",
                p.getCourse() != null ? p.getCourse().getCourseName() : "N/A",
                currentStatus,
                currentStage,
                totalStages,
                stages
        );
    }
}