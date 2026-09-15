package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.CreatePacketDTO;
import com.exam_paper.backend.dto.PacketDTO;
import com.exam_paper.backend.dto.PacketDetailDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketAttachment;
import com.exam_paper.backend.entity.PacketComment;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.*;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.entity.Marking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exam_paper.backend.dto.StatusUpdateDTO;
import com.exam_paper.backend.entity.Notification;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.io.File;
import com.exam_paper.backend.entity.AcademicCycle;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacketService {
    private final PacketRepository packetRepository;
    private final ExamPacketRepository examPacketRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PacketStatusRepository packetStatusRepository;
    private final ActivityLogService activityLogService;
    private final NotificationRepository notificationRepository;
    private final PacketAttachmentRepository packetAttachmentRepository;
    private final PacketCommentRepository packetCommentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final DelayReasonRepository delayReasonRepository;
    private final AcademicCycleRepository academicCycleRepository;
    private final AcademicCycleService academicCycleService;
    private final MarkingRepository markingRepository;

    public static String cleanCycleId(String cycleId) {
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

    public PacketStatus getOrCreateStatus(String statusName) {
        return packetStatusRepository.findByStatusName(statusName)
                .orElseGet(() -> {
                    PacketStatus ps = new PacketStatus();
                    ps.setStatusName(statusName);
                    return packetStatusRepository.save(ps);
                });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        syncMissingPacketsForCourses();
    }

    @Transactional
    public void syncMissingPacketsForCourses() {
        syncMissingPacketsForCycle(null);
    }

    @Transactional
    public void syncMissingPacketsForCycle(String cycleId) {
        try {
            // Ensure all essential statuses exist
            getOrCreateStatus("PENDING");
            getOrCreateStatus("DRAFT");
            getOrCreateStatus("SUBMITTED");
            getOrCreateStatus("APPROVED");
            getOrCreateStatus("REJECTED");
            getOrCreateStatus("PRINTING");
            getOrCreateStatus("PAPERS STORED");
            getOrCreateStatus("ANSWER SHEETS TAKEN");
            getOrCreateStatus("MARKING");
            getOrCreateStatus("COMPLETED");

            AcademicCycle activeCycle = academicCycleService.getActiveCycleEntity();

            // 1. Backfill legacy packets with null cycle to active cycle
            if (activeCycle != null) {
                List<ExamPacket> unassigned = examPacketRepository.findPacketsWithoutCycle();
                if (unassigned != null && !unassigned.isEmpty()) {
                    for (ExamPacket p : unassigned) {
                        p.setAcademicCycle(activeCycle);
                    }
                    packetRepository.saveAll(unassigned);
                }
            }

            // 2. Identify target cycle to ensure packets exist for catalog courses
            AcademicCycle targetCycle = null;
            String cleanedCycleId = cleanCycleId(cycleId);
            if (cleanedCycleId != null && !"ALL".equalsIgnoreCase(cleanedCycleId)) {
                targetCycle = academicCycleRepository.findByCycleId(cleanedCycleId).orElse(activeCycle);
            } else {
                targetCycle = activeCycle;
            }

            if (targetCycle == null) return;

            List<Course> allCourses = courseRepository.findAll();
            PacketStatus defaultStatus = getOrCreateStatus("PENDING");

            for (Course c : allCourses) {
                boolean exists = packetRepository.existsByCourse_CourseIdAndAcademicCycle_CycleId(c.getCourseId(), targetCycle.getCycleId());

                if (!exists) {
                    ExamPacket p = new ExamPacket();
                    p.setAcademicCycle(targetCycle);
                    p.setCourse(c);
                    p.setLecturer(c.getLecturer());
                    p.setModerator(c.getModerator());
                    p.setStatus(defaultStatus);
                    p.setDuration("3 Hours");
                    p.setTotalMarks(100);
                    p.setQuestions("All sections mandatory");
                    p.setFormat("Standard Exam");
                    ExamPacket saved = packetRepository.save(p);

                    String cycleLabel = " [" + (targetCycle.getCycleName() != null ? targetCycle.getCycleName() : targetCycle.getCycleId()) + "]";
                    activityLogService.logForPacket(
                            saved,
                            "PENDING",
                            "Exam packet initialized with PENDING status for course " + c.getCourseCode() + " (" + c.getCourseName() + ")" + cycleLabel,
                            "Academic Registry",
                            "AR",
                            "bg-blue-500"
                    );
                }
            }
        } catch (Exception e) {
            // Avoid blocking app startup or request if tables aren't ready yet
        }
    }

    public List<PacketDTO> getPackets(String username, String role) {
        return getPackets(username, role, null);
    }

    public List<PacketDTO> getPackets(String username, String role, String cycleId) {
        String cleaned = cleanCycleId(cycleId);
        String targetCycleId = cleaned;
        if (targetCycleId == null || targetCycleId.trim().isEmpty() || "ACTIVE".equalsIgnoreCase(targetCycleId)) {
            AcademicCycle active = academicCycleService.getActiveCycleEntity();
            targetCycleId = active != null ? active.getCycleId() : null;
        }

        syncMissingPacketsForCycle(targetCycleId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExamPacket> packets;
        boolean isAllCycles = "ALL".equalsIgnoreCase(cleaned);

        if (isAllCycles) {
            switch (role) {
                case "ROLE_ADMIN", "ROLE_SYSTEM_ADMIN" ->
                        packets = packetRepository.findAllWithDetails();
                case "ROLE_GUEST" -> {
                    if (user.getDepartment() != null) {
                        packets = packetRepository.findByDepartmentIdWithDetails(user.getDepartment().getDepartmentId());
                    } else {
                        packets = packetRepository.findAllWithDetails();
                    }
                }
                case "ROLE_USER" ->
                        packets = packetRepository.findByLecturerOrModeratorId(user.getUserId());
                case "ROLE_MODERATOR" ->
                        packets = packetRepository.findByModeratorId(user.getUserId());
                default ->
                        packets = List.of();
            }
        } else {
            switch (role) {
                case "ROLE_ADMIN", "ROLE_SYSTEM_ADMIN" ->
                        packets = packetRepository.findAllWithDetailsByCycleId(targetCycleId);
                case "ROLE_GUEST" -> {
                    if (user.getDepartment() != null) {
                        packets = packetRepository.findByDepartmentIdAndCycleId(user.getDepartment().getDepartmentId(), targetCycleId);
                    } else {
                        packets = packetRepository.findAllWithDetailsByCycleId(targetCycleId);
                    }
                }
                case "ROLE_USER" ->
                        packets = packetRepository.findByLecturerOrModeratorIdAndCycleId(user.getUserId(), targetCycleId);
                case "ROLE_MODERATOR" ->
                        packets = packetRepository.findByModeratorIdAndCycleId(user.getUserId(), targetCycleId);
                default ->
                        packets = List.of();
            }
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

        String courseCode = p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A";
        String courseName = p.getCourse() != null ? p.getCourse().getCourseName() : "N/A";
        String deptName = (p.getCourse() != null && p.getCourse().getDepartment() != null)
                ? p.getCourse().getDepartment().getDepartmentName()
                : "Unassigned";
        Long lecturerId = p.getLecturer() != null ? p.getLecturer().getUserId() : null;
        String lecturerName = p.getLecturer() != null ? p.getLecturer().getFullName() : "Unassigned";
        String lecturerUsername = p.getLecturer() != null ? p.getLecturer().getUsername() : null;

        Long moderatorId = p.getModerator() != null ? p.getModerator().getUserId() : null;
        String moderatorName = p.getModerator() != null ? p.getModerator().getFullName() : "Unassigned";
        String moderatorUsername = p.getModerator() != null ? p.getModerator().getUsername() : null;

        String statusName = p.getStatus() != null ? p.getStatus().getStatusName() : "PENDING";
        String cycleId = p.getAcademicCycle() != null ? p.getAcademicCycle().getCycleId() : null;
        String cycleName = p.getAcademicCycle() != null ? p.getAcademicCycle().getCycleName() : null;

        Integer numberOfCopies = p.getNumberOfCopies() != null ? p.getNumberOfCopies() : 50;
        Integer totalScripts = numberOfCopies;
        Integer markedScripts = 0;
        if (p.getMarking() != null) {
            if (p.getMarking().getTotalScripts() != null && p.getMarking().getTotalScripts() > 0) {
                totalScripts = p.getMarking().getTotalScripts();
            }
            if (p.getMarking().getMarkedScripts() != null) {
                markedScripts = p.getMarking().getMarkedScripts();
            }
        }
        double markingProgress = totalScripts > 0 ? ((double) markedScripts / totalScripts) * 100.0 : 0.0;

        return PacketDetailDTO.builder()
                .packetId(packetId)
                .courseCode(courseCode)
                .courseName(courseName)
                .department(deptName)
                .lecturerId(lecturerId)
                .lecturerName(lecturerName)
                .lecturerUsername(lecturerUsername)
                .moderatorId(moderatorId)
                .moderatorName(moderatorName)
                .moderatorUsername(moderatorUsername)
                .deadline(deadline)
                .moderationDeadline(p.getModerationDeadline())
                .examDate(p.getExamDate())
                .status(statusName)
                .priority(priority)
                .overdue(overdue)
                .cycleId(cycleId)
                .cycleName(cycleName)
                .duration(p.getDuration())
                .totalMarks(p.getTotalMarks())
                .questions(p.getQuestions())
                .format(p.getFormat())
                .moderatorNote(p.getModeratorNote())
                .numberOfCopies(numberOfCopies)
                .totalScripts(totalScripts)
                .markedScripts(markedScripts)
                .markingProgress(Math.round(markingProgress * 10.0) / 10.0)
                .build();
    }

    public PacketDTO toDTO(ExamPacket p) {
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

        String courseCode = p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A";
        String courseName = p.getCourse() != null ? p.getCourse().getCourseName() : "N/A";
        Long lecturerId = p.getLecturer() != null ? p.getLecturer().getUserId() : null;
        String lecturerName = p.getLecturer() != null ? p.getLecturer().getFullName() : "Unassigned";
        String lecturerUsername = p.getLecturer() != null ? p.getLecturer().getUsername() : null;

        Long moderatorId = p.getModerator() != null ? p.getModerator().getUserId() : null;
        String moderatorName = p.getModerator() != null ? p.getModerator().getFullName() : "Unassigned";
        String moderatorUsername = p.getModerator() != null ? p.getModerator().getUsername() : null;

        String statusName = p.getStatus() != null ? p.getStatus().getStatusName() : "PENDING";
        String cycleId = p.getAcademicCycle() != null ? p.getAcademicCycle().getCycleId() : null;
        String cycleName = p.getAcademicCycle() != null ? p.getAcademicCycle().getCycleName() : null;

        Integer numberOfCopies = p.getNumberOfCopies() != null ? p.getNumberOfCopies() : 50;
        Integer totalScripts = numberOfCopies;
        Integer markedScripts = 0;
        if (p.getMarking() != null) {
            if (p.getMarking().getTotalScripts() != null && p.getMarking().getTotalScripts() > 0) {
                totalScripts = p.getMarking().getTotalScripts();
            }
            if (p.getMarking().getMarkedScripts() != null) {
                markedScripts = p.getMarking().getMarkedScripts();
            }
        }
        double markingProgress = totalScripts > 0 ? ((double) markedScripts / totalScripts) * 100.0 : 0.0;

        return PacketDTO.builder()
                .id(p.getPacketId())
                .packetId(packetId)
                .courseCode(courseCode)
                .courseName(courseName)
                .lecturerId(lecturerId)
                .lecturerName(lecturerName)
                .lecturerUsername(lecturerUsername)
                .moderatorId(moderatorId)
                .moderatorName(moderatorName)
                .moderatorUsername(moderatorUsername)
                .deadline(deadline)
                .overdue(overdue)
                .status(statusName)
                .priority(priority)
                .cycleId(cycleId)
                .cycleName(cycleName)
                .numberOfCopies(numberOfCopies)
                .totalScripts(totalScripts)
                .markedScripts(markedScripts)
                .markingProgress(Math.round(markingProgress * 10.0) / 10.0)
                .build();
    }

    public PacketDTO createPacket(CreatePacketDTO dto) {
        return createPacket(dto, null);
    }

    public PacketDTO createPacket(CreatePacketDTO dto, String username) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        AcademicCycle cycle = null;
        if (dto.getCycleId() != null && !dto.getCycleId().trim().isEmpty()) {
            cycle = academicCycleRepository.findByCycleId(dto.getCycleId()).orElse(null);
        }
        if (cycle == null) {
            cycle = academicCycleService.getActiveCycleEntity();
        }

        ExamPacket existingPacket = (cycle != null)
                ? packetRepository.findByCourse_CourseIdAndAcademicCycle_CycleId(dto.getCourseId(), cycle.getCycleId()).orElse(null)
                : null;

        User lecturer = null;
        if (dto.getLecturerId() != null) {
            lecturer = userRepository.findById(dto.getLecturerId()).orElse(null);
        }
        if (lecturer == null) {
            lecturer = course.getLecturer();
        }

        User moderator = null;
        if (dto.getModeratorId() != null) {
            moderator = userRepository.findById(dto.getModeratorId()).orElse(null);
        }

        if (lecturer != null && moderator != null && lecturer.getUserId().equals(moderator.getUserId())) {
            throw new IllegalArgumentException("A lecturer cannot be assigned as the moderator for their own exam packet.");
        }

        PacketStatus status = null;
        if (dto.getStatusId() != null) {
            status = packetStatusRepository.findById(dto.getStatusId()).orElse(null);
        }
        if (status == null) {
            status = (existingPacket != null && existingPacket.getStatus() != null)
                    ? existingPacket.getStatus()
                    : getOrCreateStatus("PENDING");
        }

        ExamPacket packet;
        if (existingPacket != null) {
            packet = existingPacket;
        } else {
            packet = new ExamPacket();
            packet.setAcademicCycle(cycle);
            packet.setCourse(course);
        }

        packet.setLecturer(lecturer);
        packet.setModerator(moderator);
        packet.setStatus(status);
        packet.setDeadline(dto.getDeadline());
        packet.setModerationDeadline(dto.getModerationDeadline());
        packet.setExamDate(dto.getExamDate());
        packet.setDuration(dto.getDuration() != null && !dto.getDuration().isBlank() ? dto.getDuration() : "3 Hours");
        packet.setTotalMarks(dto.getTotalMarks() != null ? dto.getTotalMarks() : 100);
        packet.setQuestions(dto.getQuestions() != null && !dto.getQuestions().isBlank() ? dto.getQuestions() : "All sections mandatory");
        packet.setFormat(dto.getFormat() != null && !dto.getFormat().isBlank() ? dto.getFormat() : "Standard Exam");
        packet.setModeratorNote(dto.getModeratorNote());
        Integer copies = dto.getNumberOfCopies() != null && dto.getNumberOfCopies() > 0 ? dto.getNumberOfCopies() : 50;
        packet.setNumberOfCopies(copies);

        ExamPacket saved = packetRepository.save(packet);

        // Sync or initialize Marking entity
        if (lecturer != null) {
            Optional<Marking> existingMarking = markingRepository.findByPacketPacketId(saved.getPacketId());
            if (existingMarking.isEmpty()) {
                Marking m = Marking.builder()
                        .markingId("MK" + UUID.randomUUID().toString().substring(0, 8))
                        .packet(saved)
                        .lecturer(lecturer)
                        .totalScripts(copies)
                        .markedScripts(0)
                        .build();
                markingRepository.save(m);
            } else {
                Marking m = existingMarking.get();
                m.setTotalScripts(copies);
                m.setLecturer(lecturer);
                markingRepository.save(m);
            }
        }

        User creator = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        String creatorName = creator != null ? creator.getFullName() : "Academic Registry";
        String initials = creator != null ? getInitials(creator.getFullName()) : "AR";

        String lecName = lecturer != null ? lecturer.getFullName() : "Unassigned";
        String modName = moderator != null ? moderator.getFullName() : "Unassigned";
        String statusLabel = status != null ? status.getStatusName() : "DRAFT";

        // 1. Activity log
        activityLogService.logForPacket(
                saved, statusLabel,
                "Exam packet created for " + course.getCourseCode() + " (Lecturer: " + lecName + ", Moderator: " + modName + ", Copies: " + copies + ")",
                creatorName, initials, "bg-blue-500"
        );

        // 2. Notification to assigned Lecturer
        if (lecturer != null) {
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
        }

        // 3. Notification to assigned Moderator
        if (moderator != null) {
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
        }

        return toDTO(saved);
    }

    public PacketDTO updatePacket(Long id, CreatePacketDTO dto) {
        return updatePacket(id, dto, null);
    }

    public PacketDTO updatePacket(Long id, CreatePacketDTO dto, String username) {
        ExamPacket packet = packetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Packet not found"));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (packetRepository.existsByCourse_CourseIdAndPacketIdNot(dto.getCourseId(), id)) {
            throw new IllegalArgumentException("An exam packet already exists for course " + course.getCourseCode() + ".");
        }

        User lecturer = null;
        if (dto.getLecturerId() != null) {
            lecturer = userRepository.findById(dto.getLecturerId()).orElse(null);
        }
        if (lecturer == null) {
            lecturer = course.getLecturer() != null ? course.getLecturer() : packet.getLecturer();
        }

        User moderator = null;
        if (dto.getModeratorId() != null) {
            moderator = userRepository.findById(dto.getModeratorId()).orElse(null);
        } else {
            moderator = packet.getModerator();
        }

        if (lecturer != null && moderator != null && lecturer.getUserId().equals(moderator.getUserId())) {
            throw new IllegalArgumentException("A lecturer cannot be assigned as the moderator for their own exam packet.");
        }

        PacketStatus status = null;
        if (dto.getStatusId() != null) {
            status = packetStatusRepository.findById(dto.getStatusId()).orElse(null);
        }
        if (status == null) {
            status = packet.getStatus();
        }

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
        Integer updatedCopies = dto.getNumberOfCopies() != null && dto.getNumberOfCopies() > 0 ? dto.getNumberOfCopies() : (packet.getNumberOfCopies() != null ? packet.getNumberOfCopies() : 50);
        packet.setNumberOfCopies(updatedCopies);

        ExamPacket saved = packetRepository.save(packet);

        // Sync or initialize Marking entity
        if (lecturer != null) {
            Optional<Marking> existingMarking = markingRepository.findByPacketPacketId(saved.getPacketId());
            if (existingMarking.isEmpty()) {
                Marking m = Marking.builder()
                        .markingId("MK" + UUID.randomUUID().toString().substring(0, 8))
                        .packet(saved)
                        .lecturer(lecturer)
                        .totalScripts(updatedCopies)
                        .markedScripts(0)
                        .build();
                markingRepository.save(m);
            } else {
                Marking m = existingMarking.get();
                m.setTotalScripts(updatedCopies);
                m.setLecturer(lecturer);
                markingRepository.save(m);
            }
        }

        User updater = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        String updaterName = updater != null ? updater.getFullName() : "Academic Registry";
        String initials = updater != null ? getInitials(updater.getFullName()) : "AR";

        // Log to activity_log
        activityLogService.logForPacket(
                saved, saved.getStatus() != null ? saved.getStatus().getStatusName() : "DRAFT",
                "Packet details and deadlines updated by " + updaterName,
                updaterName, initials, "bg-indigo-500"
        );

        // Notify Lecturer
        if (lecturer != null) {
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
        }

        // Notify Moderator
        if (moderator != null) {
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
        }

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

        String newStatusName;
        String stageName;
        String logMessage;
        String action = dto.getAction() != null ? dto.getAction().toUpperCase() : "";

        String actorRole = actor.getRole() != null ? actor.getRole().name() : "ROLE_USER";
        boolean isPrivileged = "ROLE_ADMIN".equals(actorRole) || "ROLE_SYSTEM_ADMIN".equals(actorRole) || "ROLE_GUEST".equals(actorRole);

        if (!isPrivileged && (actor.getRole() == User.Role.ROLE_USER || actor.getRole() == User.Role.ROLE_MODERATOR)) {
            boolean isModeratorAction = "APPROVE".equalsIgnoreCase(action) || "APPROVED".equalsIgnoreCase(action) ||
                                        "REJECT".equalsIgnoreCase(action) || "REJECTED".equalsIgnoreCase(action) ||
                                        "REVISE".equalsIgnoreCase(action) || "RETURN".equalsIgnoreCase(action) ||
                                        "COMPLETE_SECOND_MARKING".equalsIgnoreCase(action) || "SECOND_MARKING_COMPLETE".equalsIgnoreCase(action);

            if (isModeratorAction) {
                if (packet.getModerator() == null || !packet.getModerator().getUserId().equals(actor.getUserId())) {
                    throw new IllegalArgumentException("Only the designated moderator can review, approve/reject, or conduct second marking for this exam packet.");
                }
            } else {
                if (packet.getLecturer() == null || !packet.getLecturer().getUserId().equals(actor.getUserId())) {
                    throw new IllegalArgumentException("Only the designated course lecturer can perform this workflow action.");
                }
            }
        }

        if ("REJECT".equalsIgnoreCase(action) || "REJECTED".equalsIgnoreCase(action) ||
            "RETURN".equalsIgnoreCase(action) || "REVISE".equalsIgnoreCase(action)) {
            if (dto.getNote() == null || dto.getNote().trim().isEmpty()) {
                throw new IllegalArgumentException("A comment is compulsory when rejecting or returning an exam packet.");
            }
        }

        if (dto.getNote() != null && !dto.getNote().trim().isEmpty()) {
            String cleanNote = dto.getNote().trim();
            packet.setModeratorNote(cleanNote);

            if ("REJECT".equalsIgnoreCase(action) || "REJECTED".equalsIgnoreCase(action) ||
                "RETURN".equalsIgnoreCase(action) || "REVISE".equalsIgnoreCase(action)) {
                PacketComment rejectionComment = PacketComment.builder()
                        .packet(packet)
                        .user(actor)
                        .comment("Moderator Feedback: " + cleanNote)
                        .createdAt(LocalDateTime.now())
                        .build();
                packetCommentRepository.save(rejectionComment);
            }
        }

        switch (action) {
            case "DRAFT", "START_DRAFT" -> {
                newStatusName = "DRAFT";
                stageName = "DRAFT";
                logMessage = "Lecturer started drafting exam paper (" + actor.getFullName() + ")";

                // Notify Lecturer
                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Drafting Started")
                            .message("Exam paper drafting for " + courseCode + " (" + courseName + ") has been initiated.")
                            .type("DRAFT")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }
            }
            case "SUBMIT", "SUBMITTED" -> {
                newStatusName = "SUBMITTED";
                stageName = "SUBMITTED";
                logMessage = "Exam paper submitted by Lecturer " + actor.getFullName() + " for moderation";

                // 1. Notify Moderator
                if (packet.getModerator() != null) {
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
                }

                // 2. Notify Lecturer (Confirmation)
                if (packet.getLecturer() != null) {
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
            }
            case "APPROVE", "APPROVED" -> {
                newStatusName = "APPROVED";
                stageName = "APPROVED";
                logMessage = "Exam paper approved by Moderator " + actor.getFullName();

                // 1. Notify Lecturer
                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Exam Paper Approved")
                            .message("Your exam paper for " + courseCode + " (" + courseName + ") has been approved by Moderator " + actor.getFullName() + "! You can now proceed to print.")
                            .type("APPROVED")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }

                // 2. Notify Moderator
                if (packet.getModerator() != null) {
                    Notification notifMod = Notification.builder()
                            .user(packet.getModerator())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Packet Approved")
                            .message(courseCode + " (" + courseName + ") has been approved and moved to the printing stage.")
                            .type("APPROVED")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifMod);
                }
            }
            case "REJECT", "REJECTED" -> {
                newStatusName = "REJECTED";
                stageName = "REJECTED";
                String feedbackNote = dto.getNote() != null ? dto.getNote().trim() : "";
                logMessage = "Exam paper rejected by Moderator " + actor.getFullName()
                        + (!feedbackNote.isEmpty() ? " — Feedback: " + feedbackNote : "");

                // Notify Lecturer (Urgent)
                if (packet.getLecturer() != null) {
                    Notification notif = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Revision Required - Paper Rejected")
                            .message("The exam paper for " + courseCode + " (" + courseName + ") was rejected by Moderator " + actor.getFullName() + (!feedbackNote.isEmpty() ? ". Feedback: " + feedbackNote : "") + ". Please review feedback and resubmit.")
                            .type("URGENT")
                            .isRead(false)
                            .isUrgent(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }
            case "RETURN" -> {
                newStatusName = "REJECTED";
                stageName = "REJECTED";
                String returnNote = dto.getNote() != null ? dto.getNote().trim() : "";
                logMessage = "Returned for revision by " + actor.getFullName()
                        + (!returnNote.isEmpty() ? " — Note: " + returnNote : "");

                // Notify Lecturer (Urgent)
                if (packet.getLecturer() != null) {
                    Notification notif = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Revision Requested")
                            .message("The exam paper for " + courseCode + " (" + courseName + ") was returned for revision by " + actor.getFullName() + (!returnNote.isEmpty() ? ". Note: " + returnNote : "") + ".")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                }
            }
            case "PRINT", "PRINTING" -> {
                newStatusName = "PRINTING";
                stageName = "PRINTING";
                logMessage = "Exam paper sent to printing by " + actor.getFullName();

                // Notify Lecturer
                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Exam Paper Printing")
                            .message("Exam paper for " + courseCode + " (" + courseName + ") has been sent to the printing queue.")
                            .type("PRINTING")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }
            }
            case "PAPERS_STORED", "PAPERS STORED", "STORE" -> {
                newStatusName = "PAPERS STORED";
                stageName = "PAPERS_STORED";
                logMessage = "Printed exam papers safely stored in custody by " + actor.getFullName();

                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Papers Stored")
                            .message("Printed exam papers for " + courseCode + " (" + courseName + ") are safely stored in custody.")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }
            }
            case "ANSWER_SHEETS_TAKEN", "ANSWER SHEETS TAKEN", "SHEETS_TAKEN" -> {
                newStatusName = "ANSWER SHEETS TAKEN";
                stageName = "ANSWER_SHEETS_TAKEN";
                logMessage = "Student answer sheets retrieved from store by " + actor.getFullName();

                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Answer Sheets Collected")
                            .message("Answer sheets for " + courseCode + " (" + courseName + ") have been retrieved for marking.")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }
            }
            case "FIRST_MARKING", "START_FIRST_MARKING", "MARKING", "START_MARKING" -> {
                newStatusName = "MARKING";
                stageName = "FIRST_MARKING";
                logMessage = "First marking started by lecturer " + actor.getFullName();

                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("First Marking in Progress")
                            .message("First marking in progress for " + courseCode + " (" + courseName + ").")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }
            }
            case "COMPLETE_FIRST_MARKING", "MARKING_COMPLETE", "MARKING COMPLETE", "SECOND_MARKING", "START_SECOND_MARKING" -> {
                newStatusName = "SECOND_MARKING";
                stageName = "SECOND_MARKING";
                logMessage = "First marking completed by lecturer " + actor.getFullName() + ". Transferred to moderator for second marking.";

                // 1. Notify Moderator for 2nd marking
                if (packet.getModerator() != null) {
                    Notification notifMod = Notification.builder()
                            .user(packet.getModerator())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Action Required: Second Marking")
                            .message("Lecturer has completed first marking for " + courseCode + " (" + courseName + "). Please proceed with second marking.")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifMod);
                }

                // 2. Notify Lecturer
                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("First Marking Completed")
                            .message("First marking for " + courseCode + " submitted. Awaiting second marking from moderator.")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }
            }
            case "COMPLETE_SECOND_MARKING", "SECOND_MARKING_COMPLETE" -> {
                newStatusName = "SECOND_MARKING_COMPLETE";
                stageName = "SECOND_MARKING_COMPLETE";
                logMessage = "Second marking completed by moderator " + actor.getFullName() + ". Returned to lecturer for finalization.";

                // 1. Notify Lecturer
                if (packet.getLecturer() != null) {
                    Notification notifLec = Notification.builder()
                            .user(packet.getLecturer())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Action Required: Finalize Exam Packet")
                            .message("Moderator has completed second marking for " + courseCode + " (" + courseName + "). You can now finalize and store the packet.")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifLec);
                }

                // 2. Notify Moderator
                if (packet.getModerator() != null) {
                    Notification notifMod = Notification.builder()
                            .user(packet.getModerator())
                            .packet(packet)
                            .courseCode(courseCode)
                            .title("Second Marking Submitted")
                            .message("You have completed second marking for " + courseCode + " (" + courseName + "). Packet returned to lecturer for finalization.")
                            .type("MODERATION")
                            .isRead(false)
                            .isUrgent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notifMod);
                }
            }
            case "COMPLETE", "COMPLETED", "FINALIZE_PACKET" -> {
                String curStatus = packet.getStatus() != null ? packet.getStatus().getStatusName() : "";
                if (!isPrivileged && !"SECOND_MARKING_COMPLETE".equalsIgnoreCase(curStatus) && !"SECOND MARKING COMPLETE".equalsIgnoreCase(curStatus)) {
                    throw new IllegalArgumentException("Cannot complete exam packet. Second marking must be completed by the assigned moderator before finalization. Current stage: " + curStatus);
                }

                newStatusName = "COMPLETED";
                stageName = "COMPLETED";
                logMessage = "Exam packet finalized and stored by " + actor.getFullName();

                // 1. Notify Lecturer
                if (packet.getLecturer() != null) {
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
                }

                // 2. Notify Moderator
                if (packet.getModerator() != null) {
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
            }
            default -> throw new RuntimeException("Invalid action: " + dto.getAction());
        }

        // Update status
        PacketStatus newStatus = getOrCreateStatus(newStatusName);
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
