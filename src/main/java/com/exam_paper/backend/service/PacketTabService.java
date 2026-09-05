package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacketTabService {

    private final PacketCommentRepository commentRepository;
    private final PacketAttachmentRepository attachmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PacketRepository packetRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogService activityLogService;

    private static final String UPLOAD_DIR = "uploads/";
    private static final List<String> AVATAR_COLORS = List.of(
            "bg-blue-500", "bg-purple-500", "bg-green-500",
            "bg-yellow-500", "bg-red-500", "bg-pink-500"
    );

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    // ─── COMMENTS ───────────────────────────────────────────

    public List<CommentDTO> getComments(Long packetId) {
        return commentRepository
                .findByPacket_PacketIdOrderByCreatedAtAsc(packetId)
                .stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO addComment(Long packetId, String text, String username) {
        ExamPacket packet = packetRepository.findById(packetId)
                .orElseThrow(() -> new RuntimeException("Packet not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PacketComment comment = PacketComment.builder()
                .packet(packet)
                .user(user)
                .comment(text)
                .createdAt(LocalDateTime.now())
                .build();

        PacketComment savedComment = commentRepository.save(comment);

        String courseCode = packet.getCourse() != null ? packet.getCourse().getCourseCode() : "Exam Packet";
        String preview = text != null && text.length() > 60 ? text.substring(0, 60) + "..." : (text != null ? text : "");

        // 1. Notify relevant user(s)
        if (packet.getLecturer() != null && user.getUserId().equals(packet.getLecturer().getUserId())) {
            // Commenter is Lecturer -> notify Moderator
            if (packet.getModerator() != null) {
                Notification notif = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New Comment on " + courseCode)
                        .message(user.getFullName() + " (Lecturer) commented: \"" + preview + "\"")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            }
        } else if (packet.getModerator() != null && user.getUserId().equals(packet.getModerator().getUserId())) {
            // Commenter is Moderator -> notify Lecturer
            if (packet.getLecturer() != null) {
                Notification notif = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New Comment on " + courseCode)
                        .message(user.getFullName() + " (Moderator) commented: \"" + preview + "\"")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            }
        } else {
            // Commenter is Registry / AR / HOD -> notify both
            if (packet.getLecturer() != null) {
                Notification notifLec = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New Comment on " + courseCode)
                        .message(user.getFullName() + ": \"" + preview + "\"")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifLec);
            }
            if (packet.getModerator() != null) {
                Notification notifMod = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New Comment on " + courseCode)
                        .message(user.getFullName() + ": \"" + preview + "\"")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifMod);
            }
        }

        // 2. Log activity
        String currentStage = packet.getStatus() != null ? packet.getStatus().getStatusName() : "DRAFT";
        activityLogService.logForPacket(
                packet, currentStage,
                "Comment posted by " + user.getFullName(),
                user.getFullName(), getInitials(user.getFullName()), "bg-purple-500"
        );

        return toCommentDTO(savedComment);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // ─── ATTACHMENTS ────────────────────────────────────────

    public List<AttachmentDTO> getAttachments(Long packetId) {
        return attachmentRepository
                .findByPacket_PacketIdOrderByUploadedAtDesc(packetId)
                .stream()
                .map(this::toAttachmentDTO)
                .collect(Collectors.toList());
    }

    public AttachmentDTO uploadAttachment(Long packetId,
                                          MultipartFile file,
                                          String username) throws IOException {
        ExamPacket packet = packetRepository.findById(packetId)
                .orElseThrow(() -> new RuntimeException("Packet not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create uploads directory
        File uploadDir = new File(UPLOAD_DIR + packetId);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = UPLOAD_DIR + packetId + "/" + fileName;
        file.transferTo(new File(filePath));

        PacketAttachment attachment = PacketAttachment.builder()
                .packet(packet)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .uploadedBy(user)
                .uploadedAt(LocalDateTime.now())
                .build();

        PacketAttachment savedAttachment = attachmentRepository.save(attachment);

        String courseCode = packet.getCourse() != null ? packet.getCourse().getCourseCode() : "Exam Packet";

        // 1. Notify relevant user(s)
        if (packet.getLecturer() != null && user.getUserId().equals(packet.getLecturer().getUserId())) {
            // Uploader is Lecturer -> notify Moderator
            if (packet.getModerator() != null) {
                Notification notif = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New File Uploaded")
                        .message(user.getFullName() + " uploaded " + file.getOriginalFilename() + " for " + courseCode + ".")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            }
        } else if (packet.getModerator() != null && user.getUserId().equals(packet.getModerator().getUserId())) {
            // Uploader is Moderator -> notify Lecturer
            if (packet.getLecturer() != null) {
                Notification notif = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New File Uploaded")
                        .message(user.getFullName() + " uploaded " + file.getOriginalFilename() + " for " + courseCode + ".")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            }
        } else {
            // Registry / AR / HOD uploaded
            if (packet.getLecturer() != null) {
                Notification notifLec = Notification.builder()
                        .user(packet.getLecturer())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New File Uploaded by Registry")
                        .message(user.getFullName() + " uploaded " + file.getOriginalFilename() + " for " + courseCode + ".")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifLec);
            }
            if (packet.getModerator() != null) {
                Notification notifMod = Notification.builder()
                        .user(packet.getModerator())
                        .packet(packet)
                        .courseCode(courseCode)
                        .title("New File Uploaded by Registry")
                        .message(user.getFullName() + " uploaded " + file.getOriginalFilename() + " for " + courseCode + ".")
                        .type("MODERATION")
                        .isUrgent(false)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notifMod);
            }
        }

        // 2. Log activity
        String currentStage = packet.getStatus() != null ? packet.getStatus().getStatusName() : "DRAFT";
        activityLogService.logForPacket(
                packet, currentStage,
                "Attachment uploaded: " + file.getOriginalFilename() + " by " + user.getFullName(),
                user.getFullName(), getInitials(user.getFullName()), "bg-indigo-500"
        );

        return toAttachmentDTO(savedAttachment);
    }

    public void deleteAttachment(Long attachmentId) {
        attachmentRepository.findById(attachmentId).ifPresent(a -> {
            new File(a.getFilePath()).delete();
            attachmentRepository.delete(a);
        });
    }

    // ─── HISTORY ────────────────────────────────────────────

    public List<HistoryDTO> getHistory(Long packetId) {
        return activityLogRepository
                .findByPacket_PacketIdOrderByCreatedAtAsc(packetId)
                .stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    // ─── MAPPERS ────────────────────────────────────────────

    private CommentDTO toCommentDTO(PacketComment c) {
        String name = c.getUser().getFullName();
        String initials = getInitials(name);
        int ci = Math.abs(name.hashCode() % AVATAR_COLORS.size());
        return new CommentDTO(
                c.getId(), c.getComment(), name, initials,
                AVATAR_COLORS.get(ci),
                c.getCreatedAt().format(FMT)
        );
    }

    private AttachmentDTO toAttachmentDTO(PacketAttachment a) {
        return new AttachmentDTO(
                a.getId(), a.getFileName(), a.getFileType(),
                formatSize(a.getFileSize()),
                a.getUploadedBy().getFullName(),
                a.getUploadedAt().format(FMT),
                "/api/packets/attachments/" + a.getId() + "/download"
        );
    }

    private HistoryDTO toHistoryDTO(ActivityLog log) {
        return new HistoryDTO(
                log.getStageName(),
                log.getMessage(),
                log.getActorName(),
                log.getActorInitials(),
                log.getActorColor(),
                log.getCreatedAt() != null ? log.getCreatedAt().format(FMT) : ""
        );
    }

    private String getInitials(String name) {
        if (name == null) return "??";
        String[] parts = name.split(" ");
        return (parts[0].substring(0, 1)
                + (parts.length > 1 ? parts[1].substring(0, 1) : "")).toUpperCase();
    }

    private String formatSize(Long bytes) {
        if (bytes == null) return "—";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}