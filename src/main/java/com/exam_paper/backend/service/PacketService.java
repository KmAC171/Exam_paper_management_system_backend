package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.CreatePacketDTO;
import com.exam_paper.backend.dto.PacketDTO;
import com.exam_paper.backend.dto.PacketDetailDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.PacketRepository;
import com.exam_paper.backend.repository.UserRepository;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.repository.CourseRepository;
import com.exam_paper.backend.repository.PacketStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.exam_paper.backend.dto.StatusUpdateDTO;
import com.exam_paper.backend.repository.NotificationRepository;
import com.exam_paper.backend.entity.Notification;
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
        return toDTO(saved);
    }

    public PacketDTO updatePacket(Long id, CreatePacketDTO dto) {
        ExamPacket packet = packetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Packet not found"));

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
        return toDTO(saved);
    }

    public void deletePacket(Long id) {
        packetRepository.deleteById(id);
    }

    public PacketDetailDTO updateStatus(Long packetId, StatusUpdateDTO dto, String username) {
        ExamPacket packet = packetRepository.findByIdWithDetails(packetId)
                .orElseThrow(() -> new RuntimeException("Packet not found"));

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Determine new status and messages
        String newStatusName;
        String stageName;
        String logMessage;
        String notifTitle;
        String notifMessage;
        String notifType;

        switch (dto.getAction()) {
            case "APPROVE" -> {
                newStatusName = "APPROVED";
                stageName = "APPROVED";
                logMessage = "Packet approved by " + actor.getFullName();
                notifTitle = "Packet Approved";
                notifMessage = packet.getCourse().getCourseCode() + " "
                        + packet.getCourse().getCourseName()
                        + " has been approved and moved to the printing queue.";
                notifType = "APPROVED";
            }
            case "RETURN" -> {
                newStatusName = "PENDING";
                stageName = "UNDER_MODERATION";
                logMessage = "Returned for revision"
                        + (dto.getNote() != null ? " — " + dto.getNote() : "")
                        + " by " + actor.getFullName();
                notifTitle = "Returned for Revision";
                notifMessage = packet.getCourse().getCourseCode() + " "
                        + packet.getCourse().getCourseName()
                        + " has been returned for revision by " + actor.getFullName() + ".";
                notifType = "MODERATION";
            }
            case "REJECT" -> {
                newStatusName = "DRAFT";
                stageName = "DRAFT";
                logMessage = "Packet rejected"
                        + (dto.getNote() != null ? " — " + dto.getNote() : "")
                        + " by " + actor.getFullName();
                notifTitle = "Packet Rejected";
                notifMessage = packet.getCourse().getCourseCode() + " "
                        + packet.getCourse().getCourseName()
                        + " has been rejected by " + actor.getFullName() + ".";
                notifType = "URGENT";
            }
            default -> throw new RuntimeException("Invalid action: " + dto.getAction());
        }

        // Update status
        PacketStatus newStatus = packetStatusRepository
                .findByStatusName(newStatusName)
                .orElseThrow(() -> new RuntimeException("Status not found: " + newStatusName));
        packet.setStatus(newStatus);
        packetRepository.save(packet);

        // Log to activity_log
        String initials = actor.getFullName().split(" ")[0].substring(0, 1)
                + (actor.getFullName().split(" ").length > 1
                ? actor.getFullName().split(" ")[1].substring(0, 1) : "");
        activityLogService.logForPacket(
                packet, stageName, logMessage,
                actor.getFullName(), initials.toUpperCase(), "bg-blue-500"
        );

        // Create notification
        Notification notification = Notification.builder()
                .title(notifTitle)
                .message(notifMessage)
                .type(notifType)
                .courseCode(packet.getCourse().getCourseCode())
                .isRead(false)
                .isUrgent("REJECT".equals(dto.getAction()))
                .createdAt(LocalDateTime.now())
                .packet(packet)
                .build();
        notificationRepository.save(notification);

        return getPacketDetail(packetId);
    }
    }
