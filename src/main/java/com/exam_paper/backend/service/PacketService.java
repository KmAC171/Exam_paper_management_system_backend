package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.CreatePacketDTO;
import com.exam_paper.backend.dto.PacketDTO;
import com.exam_paper.backend.dto.PacketDetailDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketAttachment;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.*;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.PacketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exam_paper.backend.dto.StatusUpdateDTO;
import com.exam_paper.backend.entity.Notification;
import java.io.File;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacketService {
    private final PacketRepository packetRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PacketStatusRepository packetStatusRepository;
    private final ActivityLogService activityLogService;
    private final NotificationRepository notificationRepository;
    private final PacketAttachmentRepository packetAttachmentRepository;
    private final PacketCommentRepository packetCommentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final DelayReasonRepository delayReasonRepository;

    public List<PacketDTO> getPackets(String username, String role) {
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
            default ->
                    packets = List.of();
        }

        return packets.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PacketDetailDTO getPacketDetail(Long id) {
        ExamPacket p = packetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Packet not found"));

        LocalDate today = LocalDate.now();
        LocalDate deadline = p.getDeadline();

        boolean overdue = deadline != null && deadline.isBefore(today);

        String priority;
        if (deadline == null) {
            priority = "LOW";
        } else if (deadline.isBefore(today)) {
            priority = "HIGH";
        } else if (deadline.isBefore(today.plusDays(7))) {
            priority = "MEDIUM";
        } else {
            priority = "LOW";
        }

        String packetId = String.format("PKT-%d-%03d",
                deadline != null ? deadline.getYear() : today.getYear(),
                p.getPacketId());

        return new PacketDetailDTO(
                packetId,
                p.getCourse().getCourseCode(),
                p.getCourse().getCourseName(),
                p.getCourse().getDepartment().getDepartmentName(),
                p.getLecturer().getFullName(),
                p.getModerator().getFullName(),
                deadline,
                p.getModerationDeadline(),
                p.getExamDate(),
                p.getStatus().getStatusName(),
                priority,
                overdue,
                p.getDuration(),
                p.getTotalMarks(),
                p.getQuestions(),
                p.getFormat(),
                p.getModeratorNote()
        );
    }

    private PacketDTO toDTO(ExamPacket p) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = p.getDeadline();

        boolean overdue = deadline != null && deadline.isBefore(today);

        String priority;
        if (deadline == null) {
            priority = "LOW";
        } else if (deadline.isBefore(today)) {
            priority = "HIGH";
        } else if (deadline.isBefore(today.plusDays(7))) {
            priority = "MEDIUM";
        } else {
            priority = "LOW";
        }

        // Format PKT-2026-001
        String packetId = String.format("PKT-%d-%03d",
                deadline != null ? deadline.getYear() : today.getYear(),
                p.getPacketId());

        return new PacketDTO(
                p.getPacketId(),
                packetId,
                p.getCourse().getCourseCode(),
                p.getCourse().getCourseName(),
                p.getLecturer().getFullName(),
                p.getModerator().getFullName(),
                deadline,
                overdue,
                p.getStatus().getStatusName(),
                priority
        );
    }

    public PacketDTO createPacket(CreatePacketDTO dto) {
        return createPacket(dto, null);
    }

    public PacketDTO createPacket(CreatePacketDTO dto, String username) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User lecturer = userRepository.findById(dto.getLecturerId())
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));
        User moderator = userRepository.findById(dto.getModeratorId())
                .orElseThrow(() -> new RuntimeException("Moderator not found"));
        PacketStatus status = packetStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));
        ExamPacket packet = new ExamPacket();
        packet.setCourse(course);
        packet.setLecturer(lecturer);
        packet.setModerator(moderator);
        packet.setStatus(status);
        packet.setDeadline(dto.getDeadline());
        packet.setModerationDeadline(dto.getModerationDeadline());
        packet.setExamDate(dto.getExamDate());
        packet.setDuration(dto.getDuration());
        packet.setTotalMarks(dto.getTotalMarks());
        packet.setQuestions(dto.getQuestions());
        packet.setFormat(dto.getFormat());
        packet.setModeratorNote(dto.getModeratorNote());

        ExamPacket saved = packetRepository.save(packet);

        User creator = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        String creatorName = creator != null ? creator.getFullName() : "Academic Registry";
        String initials = creator != null ? getInitials(creator.getFullName()) : "AR";

        // 1. Activity log
        activityLogService.logForPacket(
                saved, "DRAFT",
                "Exam packet created and assigned to " + lecturer.getFullName() + " (Moderator: " + moderator.getFullName() + ")",
                creatorName, initials, "bg-blue-500"
        );

        // 2. Notification to assigned Lecturer
        Notification lecturerNotif = Notification.builder()
                .user(lecturer)
                .packet(saved)
                .courseCode(course.getCourseCode())
                .title("Exam Packet Assigned")
                .message("You have been assigned to prepare the exam paper for " + course.getCourseCode() + " - " + course.getCourseName() + ". Submission deadline: " + (saved.getDeadline() != null ? saved.getDeadline().toString() : "Not specified") + ".")
                .type("MODERATION")
                .isUrgent(false)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(lecturerNotif);

        // 3. Notification to assigned Moderator
        Notification modNotif = Notification.builder()
                .user(moderator)
                .packet(saved)
                .courseCode(course.getCourseCode())
                .title("New Moderation Assignment")
                .message("You have been assigned as the moderator for " + course.getCourseCode() + " - " + course.getCourseName() + ". Moderation deadline: " + (saved.getModerationDeadline() != null ? saved.getModerationDeadline().toString() : "Not specified") + ".")
                .type("MODERATION")
                .isUrgent(false)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(modNotif);

        return toDTO(saved);
    }

    public PacketDTO updatePacket(Long id, CreatePacketDTO dto) {
        return updatePacket(id, dto, null);
    }

    public PacketDTO updatePacket(Long id, CreatePacketDTO dto, String username) {
        ExamPacket packet = packetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Packet not found"));

        User previousLecturer = packet.getLecturer();
        User previousModerator = packet.getModerator();

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User lecturer = userRepository.findById(dto.getLecturerId())
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));
        User moderator = userRepository.findById(dto.getModeratorId())
                .orElseThrow(() -> new RuntimeException("Moderator not found"));
        PacketStatus status = packetStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        packet.setCourse(course);
        packet.setLecturer(lecturer);
        packet.setModerator(moderator);
        packet.setStatus(status);
        packet.setDeadline(dto.getDeadline());
        packet.setModerationDeadline(dto.getModerationDeadline());
        packet.setExamDate(dto.getExamDate());
        packet.setDuration(dto.getDuration());
        packet.setTotalMarks(dto.getTotalMarks());
        packet.setQuestions(dto.getQuestions());
        packet.setFormat(dto.getFormat());
        packet.setModeratorNote(dto.getModeratorNote());

        ExamPacket saved = packetRepository.save(packet);

        User updater = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        String updaterName = updater != null ? updater.getFullName() : "Academic Registry";
        String initials = updater != null ? getInitials(updater.getFullName()) : "AR";

        // Log to activity_log
        activityLogService.logForPacket(
                saved, saved.getStatus().getStatusName(),
                "Packet details and deadlines updated by " + updaterName,
                updaterName, initials, "bg-indigo-500"
        );

        // Notify Lecturer
        Notification lecturerNotif = Notification.builder()
                .user(lecturer)
                .packet(saved)
                .courseCode(course.getCourseCode())
                .title("Packet Details Updated")
                .message("Exam packet details for " + course.getCourseCode() + " (" + course.getCourseName() + ") have been updated by Academic Registry. Submission deadline: " + (saved.getDeadline() != null ? saved.getDeadline().toString() : "N/A") + ".")
                .type("MODERATION")
                .isUrgent(false)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(lecturerNotif);

        // Notify Moderator
        Notification modNotif = Notification.builder()
                .user(moderator)
                .packet(saved)
                .courseCode(course.getCourseCode())
                .title("Packet Details Updated")
                .message("Exam packet details for " + course.getCourseCode() + " (" + course.getCourseName() + ") have been updated by Academic Registry. Moderation deadline: " + (saved.getModerationDeadline() != null ? saved.getModerationDeadline().toString() : "N/A") + ".")
                .type("MODERATION")
                .isUrgent(false)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(modNotif);

        return toDTO(saved);
    }

    @Transactional
    public void deletePacket(Long id) {
        ExamPacket packet = packetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Packet not found with id: " + id));

        // 1. Delete attachment files on disk and records
        List<PacketAttachment> attachments = packetAttachmentRepository.findByPacket_PacketIdOrderByUploadedAtDesc(id);
        for (PacketAttachment a : attachments) {
            if (a.getFilePath() != null) {
                try {
                    new File(a.getFilePath()).delete();
                } catch (Exception ignored) {
                }
            }
        }
        packetAttachmentRepository.deleteAll(attachments);

        // 2. Delete comments
        packetCommentRepository.deleteByPacket_PacketId(id);

        // 3. Delete activity logs
        activityLogRepository.deleteByPacket_PacketId(id);

        // 4. Delete delay reasons
        delayReasonRepository.deleteByPacket_PacketId(id);

        // 5. Delete notifications
        notificationRepository.deleteByPacket_PacketId(id);

        // 6. Delete packet
        packetRepository.delete(packet);
    }

    public PacketDetailDTO updateStatus(Long packetId, StatusUpdateDTO dto, String username) {
        ExamPacket packet = packetRepository.findByIdWithDetails(packetId)
                .orElseThrow(() -> new RuntimeException("Packet not found"));

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String courseCode = packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A";
        String courseName = packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A";

        // Determine new status and messages
        String newStatusName;
        String stageName;
        String logMessage;
        String action = dto.getAction() != null ? dto.getAction().toUpperCase() : "";

        if (dto.getNote() != null && !dto.getNote().trim().isEmpty()) {
            packet.setModeratorNote(dto.getNote().trim());
        }

        switch (action) {
            case "APPROVE" -> {
                newStatusName = "APPROVED";
                stageName = "APPROVED";
                logMessage = "Packet approved by " + actor.getFullName();

                // 1. Notify Lecturer
                Notification notifLec = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Exam Paper Approved")
                        .message("Your exam paper for " + courseCode + " (" + courseName + ") has been approved by " + actor.getFullName() + " and forwarded to the printing queue.")
                        .type("APPROVED")
                        .isRead(false)
                        .isUrgent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifLec);

                // 2. Notify Moderator
                Notification notifMod = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Packet Approved")
                        .message(courseCode + " (" + courseName + ") has been approved by " + actor.getFullName() + " and moved to the printing queue.")
                        .type("APPROVED")
                        .isRead(false)
                        .isUrgent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifMod);
            }
            case "RETURN" -> {
                newStatusName = "PENDING";
                stageName = "UNDER_MODERATION";
                logMessage = "Returned for revision"
                        + (dto.getNote() != null ? " — " + dto.getNote() : "")
                        + " by " + actor.getFullName();

                // Notify Lecturer (Urgent)
                Notification notif = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Revision Requested")
                        .message("The exam paper for " + courseCode + " (" + courseName + ") was returned for revision by " + actor.getFullName() + (dto.getNote() != null && !dto.getNote().isBlank() ? ". Note: " + dto.getNote() : "") + ".")
                        .type("MODERATION")
                        .isRead(false)
                        .isUrgent(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            }
            case "REJECT" -> {
                newStatusName = "DRAFT";
                stageName = "DRAFT";
                logMessage = "Packet rejected"
                        + (dto.getNote() != null ? " — " + dto.getNote() : "")
                        + " by " + actor.getFullName();

                // Notify Lecturer (Urgent)
                Notification notif = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Packet Rejected")
                        .message("The exam packet for " + courseCode + " (" + courseName + ") was rejected by " + actor.getFullName() + (dto.getNote() != null && !dto.getNote().isBlank() ? ". Reason: " + dto.getNote() : "") + ".")
                        .type("URGENT")
                        .isRead(false)
                        .isUrgent(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            }
            case "SUBMIT", "SUBMITTED" -> {
                newStatusName = "PENDING";
                stageName = "SUBMITTED";
                logMessage = "Exam paper submitted by " + actor.getFullName();

                // 1. Notify Moderator
                Notification notifMod = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Exam Paper Submitted for Moderation")
                        .message("Lecturer " + actor.getFullName() + " has submitted the exam paper for " + courseCode + " (" + courseName + "). Please review and complete moderation.")
                        .type("MODERATION")
                        .isRead(false)
                        .isUrgent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifMod);

                // 2. Notify Lecturer (Confirmation)
                Notification notifLec = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Exam Paper Submitted")
                        .message("Your exam paper for " + courseCode + " (" + courseName + ") was successfully submitted for moderation.")
                        .type("MODERATION")
                        .isRead(false)
                        .isUrgent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifLec);
            }
            case "COMPLETE", "COMPLETED" -> {
                newStatusName = "COMPLETED";
                stageName = "COMPLETED";
                logMessage = "Task marked completed by " + actor.getFullName();

                // 1. Notify Lecturer
                Notification notifLec = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Exam Packet Completed")
                        .message("The exam packet workflow for " + courseCode + " (" + courseName + ") has been completed.")
                        .type("COMPLETED")
                        .isRead(false)
                        .isUrgent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifLec);

                // 2. Notify Moderator
                Notification notifMod = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("Exam Packet Completed")
                        .message("The exam packet workflow for " + courseCode + " (" + courseName + ") has been completed.")
                        .type("COMPLETED")
                        .isRead(false)
                        .isUrgent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifMod);
            }
            default -> throw new RuntimeException("Invalid action: " + dto.getAction());
        }

        // Update status
        PacketStatus newStatus = packetStatusRepository
                .findByStatusName(newStatusName)
                .or(() -> packetStatusRepository.findByStatusName("PENDING"))
                .orElseThrow(() -> new RuntimeException("Status not found: " + newStatusName));
        packet.setStatus(newStatus);
        packetRepository.save(packet);

        // Log to activity_log
        String initials = getInitials(actor.getFullName());
        activityLogService.logForPacket(
                packet, stageName, logMessage,
                actor.getFullName(), initials, "bg-blue-500"
        );

        return getPacketDetail(packetId);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}
