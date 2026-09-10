package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.printing.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.entity.PrintingSchedule;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketStatusRepository;
import com.exam_paper.backend.repository.PrintingScheduleRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrintingScheduleService {

    private final PrintingScheduleRepository printingScheduleRepository;
    private final ExamPacketRepository examPacketRepository;
    private final UserRepository userRepository;
    private final PacketStatusRepository packetStatusRepository;
    private final NotificationService notificationService;

    public static final String DEFAULT_LOCATION = "Exam Printing Center - Room 102";
    private static final LocalTime WORK_START = LocalTime.of(8, 30);
    private static final LocalTime WORK_END = LocalTime.of(16, 30);
    private static final int SLOT_MINUTES = 30;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Calculates the time-slot availability grid for a specific date and location.
     */
    public List<SlotAvailabilityDTO> getAvailableSlots(LocalDate date, String location) {
        final String effectiveLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : DEFAULT_LOCATION;
        final LocalDate effectiveDate = (date != null) ? date : LocalDate.now();

        List<String> excludedStatuses = List.of("CANCELLED", "MISSED");
        List<PrintingSchedule> activeBookings = printingScheduleRepository
                .findByScheduleDateAndLocationAndStatusNotIn(effectiveDate, effectiveLocation, excludedStatuses);

        List<SlotAvailabilityDTO> slots = new ArrayList<>();
        LocalTime currentStart = WORK_START;

        while (currentStart.plusMinutes(SLOT_MINUTES).compareTo(WORK_END) <= 0) {
            LocalTime currentEnd = currentStart.plusMinutes(SLOT_MINUTES);

            final LocalTime slotStart = currentStart;
            final LocalTime slotEnd = currentEnd;

            // Check if any booking overlaps with this slot
            Optional<PrintingSchedule> overlapping = activeBookings.stream()
                    .filter(b -> b.getStartTime().isBefore(slotEnd) && b.getEndTime().isAfter(slotStart))
                    .findFirst();

            String timeLabel = slotStart.format(TIME_FMT) + " - " + slotEnd.format(TIME_FMT);

            if (overlapping.isPresent()) {
                PrintingSchedule b = overlapping.get();
                ExamPacket p = b.getPacket();
                String courseCode = (p != null && p.getCourse() != null) ? p.getCourse().getCourseCode() : "";
                String courseName = (p != null && p.getCourse() != null) ? p.getCourse().getCourseName() : "";
                String lecturerName = (b.getLecturer() != null) ? b.getLecturer().getFullName() : "";

                slots.add(SlotAvailabilityDTO.builder()
                        .date(effectiveDate)
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .timeLabel(timeLabel)
                        .isAvailable(false)
                        .location(effectiveLocation)
                        .bookedScheduleId(b.getScheduleId())
                        .bookedPacketId(p != null ? p.getPacketId() : null)
                        .bookedCourseCode(courseCode)
                        .bookedCourseName(courseName)
                        .bookedLecturerName(lecturerName)
                        .build());
            } else {
                slots.add(SlotAvailabilityDTO.builder()
                        .date(effectiveDate)
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .timeLabel(timeLabel)
                        .isAvailable(true)
                        .location(effectiveLocation)
                        .build());
            }

            currentStart = currentEnd;
        }

        return slots;
    }

    /**
     * Book a printing slot with strict concurrency/overlap prevention.
     */
    @Transactional
    public PrintingScheduleResponseDTO bookSlot(String username, BookSlotRequestDTO request) {
        if (request.getPacketId() == null) {
            throw new IllegalArgumentException("Packet ID is required.");
        }
        if (request.getScheduleDate() == null) {
            throw new IllegalArgumentException("Schedule date is required.");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start and End times are required.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        ExamPacket packet = examPacketRepository.findById(request.getPacketId())
                .orElseThrow(() -> new IllegalArgumentException("Exam packet not found with ID: " + request.getPacketId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if caller is authorized
        boolean isSystemAdmin = user.getRole() == User.Role.ROLE_SYSTEM_ADMIN;
        boolean isAdmin = user.getRole() == User.Role.ROLE_ADMIN;
        boolean isAssignedLecturer = packet.getLecturer() != null && packet.getLecturer().getUserId().equals(user.getUserId());

        if (!isSystemAdmin && !isAdmin && !isAssignedLecturer) {
            throw new IllegalArgumentException("You are not authorized to book a printing slot for this exam packet.");
        }

        String location = (request.getLocation() != null && !request.getLocation().trim().isEmpty())
                ? request.getLocation().trim()
                : DEFAULT_LOCATION;

        // Strict mutual exclusion / conflict validation
        List<PrintingSchedule> overlaps = printingScheduleRepository.findOverlappingActiveSchedules(
                request.getScheduleDate(),
                location,
                request.getStartTime(),
                request.getEndTime(),
                null
        );

        if (!overlaps.isEmpty()) {
            PrintingSchedule conflict = overlaps.get(0);
            String bookedCourse = (conflict.getPacket() != null && conflict.getPacket().getCourse() != null)
                    ? conflict.getPacket().getCourse().getCourseCode()
                    : "another paper";
            throw new IllegalStateException("Slot conflict: The time slot " +
                    request.getStartTime().format(TIME_FMT) + " - " + request.getEndTime().format(TIME_FMT) +
                    " on " + request.getScheduleDate() + " at " + location +
                    " is already reserved for " + bookedCourse + ". Please select another slot.");
        }

        // Check if this packet already has an active scheduled booking
        List<PrintingSchedule> existingActive = printingScheduleRepository
                .findByPacketPacketIdAndStatusNotIn(packet.getPacketId(), List.of("CANCELLED", "MISSED", "COMPLETED"));
        if (!existingActive.isEmpty()) {
            PrintingSchedule active = existingActive.get(0);
            throw new IllegalStateException("This exam packet already has an active printing appointment on " +
                    active.getScheduleDate() + " at " + active.getStartTime().format(TIME_FMT) +
                    ". Please reschedule the existing appointment if you wish to change the time.");
        }

        User assignedLecturer = packet.getLecturer() != null ? packet.getLecturer() : user;

        PrintingSchedule schedule = PrintingSchedule.builder()
                .packet(packet)
                .lecturer(assignedLecturer)
                .scheduleDate(request.getScheduleDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(location)
                .status("SCHEDULED")
                .copies(request.getCopies() != null ? request.getCopies() : 50)
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PrintingSchedule saved = printingScheduleRepository.save(schedule);

        // Advance packet status to PRINTING if in APPROVED or earlier
        String currentPacketStatus = packet.getStatus() != null ? packet.getStatus().getStatusName() : "APPROVED";
        if ("APPROVED".equalsIgnoreCase(currentPacketStatus) || "DRAFT".equalsIgnoreCase(currentPacketStatus)) {
            PacketStatus printingStatus = packetStatusRepository.findByStatusName("PRINTING")
                    .orElseGet(() -> {
                        PacketStatus ps = new PacketStatus();
                        ps.setStatusName("PRINTING");
                        return packetStatusRepository.save(ps);
                    });
            packet.setStatus(printingStatus);
            examPacketRepository.save(packet);
        }

        // Trigger Notifications
        String courseCode = (packet.getCourse() != null) ? packet.getCourse().getCourseCode() : ("Packet #" + packet.getPacketId());
        String timeStr = request.getScheduleDate() + " (" + request.getStartTime().format(TIME_FMT) + " - " + request.getEndTime().format(TIME_FMT) + ")";

        // Notify Lecturer
        if (assignedLecturer != null) {
            notificationService.createNotification(
                    assignedLecturer,
                    packet,
                    "Printing Slot Confirmed",
                    "Your printing appointment for " + courseCode + " has been confirmed for " + timeStr + " at " + location + ".",
                    "PRINTING",
                    false
            );
        }

        // Notify AR & HOD
        notificationService.notifyAR(
                "New Printing Slot Booked",
                assignedLecturer.getFullName() + " booked a printing slot for " + courseCode + " on " + timeStr + ".",
                "PRINTING",
                false,
                username
        );

        return mapToDTO(saved);
    }

    /**
     * Reschedule an existing printing slot atomically.
     */
    @Transactional
    public PrintingScheduleResponseDTO rescheduleSlot(Long scheduleId, String username, RescheduleSlotRequestDTO request) {
        PrintingSchedule schedule = printingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Printing schedule not found with ID: " + scheduleId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        boolean isSystemAdmin = user.getRole() == User.Role.ROLE_SYSTEM_ADMIN;
        boolean isAdmin = user.getRole() == User.Role.ROLE_ADMIN;
        boolean isOwner = schedule.getLecturer() != null && schedule.getLecturer().getUserId().equals(user.getUserId());

        if (!isSystemAdmin && !isAdmin && !isOwner) {
            throw new IllegalArgumentException("You are not authorized to reschedule this printing appointment.");
        }

        if ("COMPLETED".equalsIgnoreCase(schedule.getStatus()) || "CANCELLED".equalsIgnoreCase(schedule.getStatus())) {
            throw new IllegalStateException("Cannot reschedule a " + schedule.getStatus() + " appointment.");
        }

        String location = (request.getLocation() != null && !request.getLocation().trim().isEmpty())
                ? request.getLocation().trim()
                : schedule.getLocation();

        // Check for conflicts excluding current schedule
        List<PrintingSchedule> overlaps = printingScheduleRepository.findOverlappingActiveSchedules(
                request.getScheduleDate(),
                location,
                request.getStartTime(),
                request.getEndTime(),
                schedule.getScheduleId()
        );

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("The requested time slot " +
                    request.getStartTime().format(TIME_FMT) + " - " + request.getEndTime().format(TIME_FMT) +
                    " on " + request.getScheduleDate() + " is already reserved. Please pick another slot.");
        }

        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLocation(location);
        if (request.getCopies() != null) schedule.setCopies(request.getCopies());
        if (request.getNotes() != null) schedule.setNotes(request.getNotes());
        schedule.setStatus("SCHEDULED");
        schedule.setUpdatedAt(LocalDateTime.now());

        PrintingSchedule saved = printingScheduleRepository.save(schedule);

        ExamPacket packet = schedule.getPacket();
        String courseCode = (packet != null && packet.getCourse() != null) ? packet.getCourse().getCourseCode() : "";
        String timeStr = saved.getScheduleDate() + " (" + saved.getStartTime().format(TIME_FMT) + " - " + saved.getEndTime().format(TIME_FMT) + ")";

        if (schedule.getLecturer() != null) {
            notificationService.createNotification(
                    schedule.getLecturer(),
                    packet,
                    "Printing Appointment Rescheduled",
                    "Your printing appointment for " + courseCode + " has been updated to " + timeStr + " at " + location + ".",
                    "PRINTING",
                    false
            );
        }

        return mapToDTO(saved);
    }

    /**
     * Cancel an existing printing slot.
     */
    @Transactional
    public void cancelSlot(Long scheduleId, String username) {
        PrintingSchedule schedule = printingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Printing schedule not found with ID: " + scheduleId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        boolean isSystemAdmin = user.getRole() == User.Role.ROLE_SYSTEM_ADMIN;
        boolean isAdmin = user.getRole() == User.Role.ROLE_ADMIN;
        boolean isOwner = schedule.getLecturer() != null && schedule.getLecturer().getUserId().equals(user.getUserId());

        if (!isSystemAdmin && !isAdmin && !isOwner) {
            throw new IllegalArgumentException("You are not authorized to cancel this printing appointment.");
        }

        schedule.setStatus("CANCELLED");
        schedule.setUpdatedAt(LocalDateTime.now());
        printingScheduleRepository.save(schedule);

        ExamPacket packet = schedule.getPacket();
        String courseCode = (packet != null && packet.getCourse() != null) ? packet.getCourse().getCourseCode() : "";

        if (schedule.getLecturer() != null && !schedule.getLecturer().getUsername().equalsIgnoreCase(username)) {
            notificationService.createNotification(
                    schedule.getLecturer(),
                    packet,
                    "Printing Appointment Cancelled",
                    "The printing slot for " + courseCode + " on " + schedule.getScheduleDate() + " was cancelled.",
                    "PRINTING",
                    true
            );
        }
    }

    /**
     * Update printing status (e.g. IN_PROGRESS, COMPLETED, DELAYED).
     */
    @Transactional
    public PrintingScheduleResponseDTO updateStatus(Long scheduleId, UpdatePrintingStatusDTO request, String username) {
        PrintingSchedule schedule = printingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Printing schedule not found with ID: " + scheduleId));

        String newStatus = request.getStatus().toUpperCase();
        schedule.setStatus(newStatus);
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            schedule.setNotes(request.getNotes());
        }
        schedule.setUpdatedAt(LocalDateTime.now());

        PrintingSchedule saved = printingScheduleRepository.save(schedule);
        ExamPacket packet = schedule.getPacket();

        // If marked as COMPLETED, optionally move packet to PAPERS STORED
        if ("COMPLETED".equalsIgnoreCase(newStatus) && packet != null) {
            PacketStatus storedStatus = packetStatusRepository.findByStatusName("PAPERS STORED")
                    .orElseGet(() -> {
                        PacketStatus ps = new PacketStatus();
                        ps.setStatusName("PAPERS STORED");
                        return packetStatusRepository.save(ps);
                    });
            packet.setStatus(storedStatus);
            examPacketRepository.save(packet);

            String courseCode = (packet.getCourse() != null) ? packet.getCourse().getCourseCode() : "";
            if (schedule.getLecturer() != null) {
                notificationService.createNotification(
                        schedule.getLecturer(),
                        packet,
                        "Exam Paper Printed & Ready",
                        "The physical exam papers for " + courseCode + " have been successfully printed and prepared for storage.",
                        "PRINTING",
                        false
                );
            }
        } else if ("DELAYED".equalsIgnoreCase(newStatus) && packet != null) {
            String courseCode = (packet.getCourse() != null) ? packet.getCourse().getCourseCode() : "";
            if (schedule.getLecturer() != null) {
                notificationService.createNotification(
                        schedule.getLecturer(),
                        packet,
                        "Printing Delay Alert",
                        "Printing for " + courseCode + " is experiencing a delay at the printing facility. Notes: " + (request.getNotes() != null ? request.getNotes() : "Under review"),
                        "PRINTING",
                        true
                );
            }
        }

        return mapToDTO(saved);
    }

    /**
     * Get all schedules with date range, status, or search filters.
     */
    public List<PrintingScheduleResponseDTO> getAllSchedules(LocalDate fromDate, LocalDate toDate, String status, Long departmentId) {
        LocalDate start = (fromDate != null) ? fromDate : LocalDate.now().minusDays(7);
        LocalDate end = (toDate != null) ? toDate : LocalDate.now().plusMonths(1);

        List<PrintingSchedule> list = printingScheduleRepository.findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(start, end);

        return list.stream()
                .filter(s -> status == null || "ALL".equalsIgnoreCase(status) || s.getStatus().equalsIgnoreCase(status))
                .filter(s -> {
                    if (departmentId == null) return true;
                    ExamPacket p = s.getPacket();
                    return p != null && p.getCourse() != null && p.getCourse().getDepartment() != null
                            && departmentId.equals(p.getCourse().getDepartment().getDepartmentId());
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get schedules for the currently logged in lecturer.
     */
    public List<PrintingScheduleResponseDTO> getMySchedules(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        List<PrintingSchedule> list = printingScheduleRepository.findByLecturerOrderByScheduleDateDescStartTimeDesc(user);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Get active schedule for a specific packet ID.
     */
    public Optional<PrintingScheduleResponseDTO> getActiveScheduleForPacket(Long packetId) {
        return printingScheduleRepository
                .findFirstByPacketPacketIdAndStatusNotInOrderByCreatedAtDesc(packetId, List.of("CANCELLED", "MISSED"))
                .map(this::mapToDTO);
    }

    /**
     * Statistics for printing queue dashboard.
     */
    public Map<String, Object> getPrintingStats() {
        LocalDate today = LocalDate.now();
        long todayTotal = printingScheduleRepository.countByScheduleDate(today);
        long todayScheduled = printingScheduleRepository.countByScheduleDateAndStatus(today, "SCHEDULED");
        long todayInProgress = printingScheduleRepository.countByScheduleDateAndStatus(today, "IN_PROGRESS");
        long todayCompleted = printingScheduleRepository.countByScheduleDateAndStatus(today, "COMPLETED");
        long totalCompleted = printingScheduleRepository.countByStatus("COMPLETED");

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayTotal", todayTotal);
        stats.put("todayScheduled", todayScheduled);
        stats.put("todayInProgress", todayInProgress);
        stats.put("todayCompleted", todayCompleted);
        stats.put("totalCompleted", totalCompleted);
        return stats;
    }

    private PrintingScheduleResponseDTO mapToDTO(PrintingSchedule s) {
        ExamPacket p = s.getPacket();
        String courseCode = (p != null && p.getCourse() != null) ? p.getCourse().getCourseCode() : "N/A";
        String courseName = (p != null && p.getCourse() != null) ? p.getCourse().getCourseName() : "N/A";
        String deptName = (p != null && p.getCourse() != null && p.getCourse().getDepartment() != null)
                ? p.getCourse().getDepartment().getDepartmentName()
                : "N/A";
        String packetStatus = (p != null && p.getStatus() != null) ? p.getStatus().getStatusName() : "UNKNOWN";
        LocalDate examDate = (p != null) ? p.getExamDate() : null;

        User lecturer = s.getLecturer();
        String lecturerName = lecturer != null ? lecturer.getFullName() : "Unassigned";
        String lecturerEmail = lecturer != null ? lecturer.getEmail() : "";
        Long lecturerId = lecturer != null ? lecturer.getUserId() : null;

        String timeLabel = (s.getStartTime() != null && s.getEndTime() != null)
                ? (s.getStartTime().format(TIME_FMT) + " - " + s.getEndTime().format(TIME_FMT))
                : "";

        return PrintingScheduleResponseDTO.builder()
                .scheduleId(s.getScheduleId())
                .packetId(p != null ? p.getPacketId() : null)
                .courseCode(courseCode)
                .courseName(courseName)
                .departmentName(deptName)
                .lecturerId(lecturerId)
                .lecturerName(lecturerName)
                .lecturerEmail(lecturerEmail)
                .scheduleDate(s.getScheduleDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .timeLabel(timeLabel)
                .location(s.getLocation())
                .status(s.getStatus())
                .copies(s.getCopies())
                .notes(s.getNotes())
                .examDate(examDate)
                .packetStatus(packetStatus)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
