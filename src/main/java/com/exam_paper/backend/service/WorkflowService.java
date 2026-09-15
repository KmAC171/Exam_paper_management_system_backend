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
    private final PacketService packetService;

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
            new StageDefinition("FIRST_MARKING", "First Marking", "Lecturer grading",
                    List.of("FIRST_MARKING", "FIRST MARKING", "1ST_MARKING", "MARKING", "UNDER_MARKING", "START_MARKING")),
            new StageDefinition("SECOND_MARKING", "Second Marking", "Moderator second marking",
                    List.of("SECOND_MARKING", "SECOND MARKING", "2ND_MARKING", "UNDER_SECOND_MARKING", "START_SECOND_MARKING", "SECOND_MARKING_COMPLETE", "SECOND MARKING COMPLETE", "MODERATOR_MARKING")),
            new StageDefinition("COMPLETED", "Completed", "Archived & Finalized",
                    List.of("COMPLETED", "COMPLETE", "FINALIZED", "DONE"))
    );

    public static int getStageIndexForStatus(String statusName) {
        if (statusName == null) return 0;
        String clean = statusName.trim().toUpperCase();
        for (int i = 0; i < WORKFLOW_STAGES.size(); i++) {
            if (WORKFLOW_STAGES.get(i).matchingStatuses().contains(clean)) {
                return i;
            }
        }
        return 0;
    }

    public List<WorkflowPacketDTO> getWorkflowPackets(String username, String role) {
        return getWorkflowPackets(username, role, null);
    }

    public List<WorkflowPacketDTO> getWorkflowPackets(String username, String role, String cycleId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String cleanedCycleId = PacketService.cleanCycleId(cycleId);
        String effectiveCycleId = (cleanedCycleId != null && !"ALL".equalsIgnoreCase(cleanedCycleId)) ? cleanedCycleId : null;

        packetService.syncMissingPacketsForCycle(effectiveCycleId);

        String userRole = user.getRole() != null ? user.getRole().name() : (role != null ? role : "ROLE_ADMIN");

        List<ExamPacket> packets;
        switch (userRole) {
            case "ROLE_ADMIN", "ROLE_GUEST", "ROLE_SYSTEM_ADMIN" ->
                    packets = packetRepository.findAllWithDetails();
            case "ROLE_USER" ->
                    packets = packetRepository.findByLecturerOrModeratorId(user.getUserId());
            case "ROLE_MODERATOR" ->
                    packets = packetRepository.findByModeratorId(user.getUserId());
            default ->
                    packets = List.of();
        }

        if (effectiveCycleId != null) {
            packets = packets.stream()
                    .filter(p -> p.getAcademicCycle() != null && effectiveCycleId.equalsIgnoreCase(p.getAcademicCycle().getCycleId()))
                    .collect(Collectors.toList());
        }

        return packets.stream()
                .map(this::toWorkflowDTO)
                .collect(Collectors.toList());
    }

    private WorkflowPacketDTO toWorkflowDTO(ExamPacket p) {
        String currentStatus = p.getStatus() != null ? p.getStatus().getStatusName() : "PENDING";
        int currentStageIndex = getStageIndexForStatus(currentStatus);

        boolean isPacketCompleted = "COMPLETED".equalsIgnoreCase(currentStatus)
                || "COMPLETE".equalsIgnoreCase(currentStatus)
                || "FINALIZED".equalsIgnoreCase(currentStatus)
                || "DONE".equalsIgnoreCase(currentStatus);

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

        return WorkflowPacketDTO.builder()
                .id(p.getPacketId())
                .packetId(packetIdStr)
                .courseCode(p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A")
                .courseName(p.getCourse() != null ? p.getCourse().getCourseName() : "N/A")
                .status(currentStatus)
                .currentStage(currentStage)
                .totalStages(totalStages)
                .lecturerUsername(p.getLecturer() != null ? p.getLecturer().getUsername() : null)
                .lecturerName(p.getLecturer() != null ? p.getLecturer().getFullName() : null)
                .moderatorUsername(p.getModerator() != null ? p.getModerator().getUsername() : null)
                .moderatorName(p.getModerator() != null ? p.getModerator().getFullName() : null)
                .stages(stages)
                .build();
    }
}