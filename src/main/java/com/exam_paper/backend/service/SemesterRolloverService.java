package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.AcademicCycleDTO;
import com.exam_paper.backend.dto.SemesterRolloverDTO.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemesterRolloverService {

    private final AcademicCycleRepository academicCycleRepository;
    private final AcademicCycleService academicCycleService;
    private final CourseRepository courseRepository;
    private final PacketRepository packetRepository;
    private final PacketStatusRepository packetStatusRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public RolloverPreviewResponse previewRollover(RolloverRequest request) {
        String sourceCycleId = request.getSourceCycleId();
        AcademicCycle sourceCycle = null;
        if (sourceCycleId != null && !sourceCycleId.trim().isEmpty()) {
            sourceCycle = academicCycleRepository.findByCycleId(sourceCycleId).orElse(null);
        }

        LocalDate targetStart = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().plusMonths(1);
        LocalDate targetEnd = request.getEndDate() != null ? request.getEndDate() : targetStart.plusMonths(5);

        long dayOffset = 0;
        if (sourceCycle != null && sourceCycle.getStartDate() != null && targetStart != null) {
            dayOffset = ChronoUnit.DAYS.between(sourceCycle.getStartDate(), targetStart);
        }

        List<RolloverPreviewItem> items = new ArrayList<>();

        if (sourceCycle != null) {
            List<ExamPacket> sourcePackets = packetRepository.findByAcademicCycle_CycleId(sourceCycle.getCycleId());
            for (ExamPacket p : sourcePackets) {
                Course c = p.getCourse();
                if (c == null) continue;

                LocalDate deadline = (p.getDeadline() != null && request.isAutoShiftDeadlines())
                        ? p.getDeadline().plusDays(dayOffset)
                        : targetStart.plusWeeks(6);

                LocalDate modDeadline = (p.getModerationDeadline() != null && request.isAutoShiftDeadlines())
                        ? p.getModerationDeadline().plusDays(dayOffset)
                        : targetStart.plusWeeks(8);

                User lecturer = request.isKeepStaffAssignments() ? p.getLecturer() : c.getLecturer();
                User moderator = request.isKeepStaffAssignments() ? p.getModerator() : c.getModerator();

                items.add(RolloverPreviewItem.builder()
                        .courseId(c.getCourseId())
                        .courseCode(c.getCourseCode())
                        .courseName(c.getCourseName())
                        .departmentName(c.getDepartment() != null ? c.getDepartment().getDepartmentName() : "Unassigned")
                        .lecturerId(lecturer != null ? lecturer.getUserId() : null)
                        .lecturerName(lecturer != null ? lecturer.getFullName() : "Unassigned")
                        .moderatorId(moderator != null ? moderator.getUserId() : null)
                        .moderatorName(moderator != null ? moderator.getFullName() : "Unassigned")
                        .calculatedDeadline(deadline)
                        .calculatedModerationDeadline(modDeadline)
                        .defaultDuration(p.getDuration() != null ? p.getDuration() : "3 Hours")
                        .defaultTotalMarks(p.getTotalMarks() != null ? p.getTotalMarks() : 100)
                        .build());
            }
        }

        // Fallback: If source cycle has no packets or wasn't provided, build from master courses
        if (items.isEmpty()) {
            List<Course> allCourses = courseRepository.findAllWithDepartmentOrderByCourseCodeAsc();
            for (Course c : allCourses) {
                // Filter by target semester if specified in course defaultSemester (or if defaultSemester == 3 "Both")
                if (request.getTargetSemester() != null && c.getDefaultSemester() != null) {
                    if (c.getDefaultSemester() != 3 && !c.getDefaultSemester().equals(request.getTargetSemester())) {
                        continue;
                    }
                }

                items.add(RolloverPreviewItem.builder()
                        .courseId(c.getCourseId())
                        .courseCode(c.getCourseCode())
                        .courseName(c.getCourseName())
                        .departmentName(c.getDepartment() != null ? c.getDepartment().getDepartmentName() : "Unassigned")
                        .lecturerId(c.getLecturer() != null ? c.getLecturer().getUserId() : null)
                        .lecturerName(c.getLecturer() != null ? c.getLecturer().getFullName() : "Unassigned")
                        .moderatorId(c.getModerator() != null ? c.getModerator().getUserId() : null)
                        .moderatorName(c.getModerator() != null ? c.getModerator().getFullName() : "Unassigned")
                        .calculatedDeadline(targetStart.plusWeeks(6))
                        .calculatedModerationDeadline(targetStart.plusWeeks(8))
                        .defaultDuration("3 Hours")
                        .defaultTotalMarks(100)
                        .build());
            }
        }

        String targetCycleId = "AY" + (request.getTargetAcademicYear() != null ? request.getTargetAcademicYear().replaceAll("[^0-9]", "-") : "NEW") +
                "-SEM" + (request.getTargetSemester() != null ? request.getTargetSemester() : "1");

        String targetCycleName = request.getTargetCycleName() != null && !request.getTargetCycleName().trim().isEmpty()
                ? request.getTargetCycleName().trim()
                : (request.getTargetAcademicYear() != null ? request.getTargetAcademicYear() : "") + " - Semester " + (request.getTargetSemester() != null ? request.getTargetSemester() : 1);

        return RolloverPreviewResponse.builder()
                .sourceCycleId(sourceCycle != null ? sourceCycle.getCycleId() : "MASTER_CATALOG")
                .sourceCycleName(sourceCycle != null ? sourceCycle.getCycleName() : "Master Course Catalog")
                .targetCycleId(targetCycleId)
                .targetCycleName(targetCycleName)
                .targetStartDate(targetStart)
                .targetEndDate(targetEnd)
                .totalCoursesToClone(items.size())
                .items(items)
                .build();
    }

    @Transactional
    public RolloverExecutionResult executeRollover(RolloverRequest request, String username) {
        if (request.getTargetAcademicYear() == null || request.getTargetAcademicYear().trim().isEmpty()) {
            throw new IllegalArgumentException("Target Academic Year is required (e.g. 2026/2027)");
        }
        if (request.getTargetSemester() == null || (request.getTargetSemester() != 1 && request.getTargetSemester() != 2)) {
            throw new IllegalArgumentException("Target Semester must be 1 or 2");
        }

        LocalDate targetStart = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().plusMonths(1);
        LocalDate targetEnd = request.getEndDate() != null ? request.getEndDate() : targetStart.plusMonths(5);

        String yearClean = request.getTargetAcademicYear().replaceAll("[^0-9]", "-");
        String targetCycleId = "AY" + yearClean + "-SEM" + request.getTargetSemester();
        String targetCycleName = request.getTargetCycleName() != null && !request.getTargetCycleName().trim().isEmpty()
                ? request.getTargetCycleName().trim()
                : request.getTargetAcademicYear() + " - Semester " + request.getTargetSemester();

        // 1. Ensure target AcademicCycle exists
        AcademicCycle targetCycle = academicCycleRepository.findByCycleId(targetCycleId).orElseGet(() -> {
            AcademicCycle newCycle = AcademicCycle.builder()
                    .cycleId(targetCycleId)
                    .academicYear(request.getTargetAcademicYear().trim())
                    .year(targetStart.getYear())
                    .semester(request.getTargetSemester())
                    .cycleName(targetCycleName)
                    .startDate(targetStart)
                    .endDate(targetEnd)
                    .status("PLANNED")
                    .build();
            return academicCycleRepository.save(newCycle);
        });

        // 2. Fetch preview items
        RolloverPreviewResponse preview = previewRollover(request);
        List<RolloverPreviewItem> itemsToProcess = preview.getItems();

        // If specific course IDs were selected, filter to those
        if (request.getIncludedCourseIds() != null && !request.getIncludedCourseIds().isEmpty()) {
            Set<Long> includedSet = new HashSet<>(request.getIncludedCourseIds());
            itemsToProcess = itemsToProcess.stream()
                    .filter(i -> includedSet.contains(i.getCourseId()))
                    .collect(Collectors.toList());
        }

        // Map any custom staff overrides
        Map<Long, CourseStaffOverride> overrideMap = new HashMap<>();
        if (request.getStaffOverrides() != null) {
            for (CourseStaffOverride o : request.getStaffOverrides()) {
                if (o.getCourseId() != null) {
                    overrideMap.put(o.getCourseId(), o);
                }
            }
        }

        PacketStatus pendingStatus = packetStatusRepository.findByStatusName("PENDING")
                .or(() -> packetStatusRepository.findByStatusName("DRAFT"))
                .orElse(null);

        int createdCount = 0;
        int assignedLecturers = 0;
        int assignedModerators = 0;

        for (RolloverPreviewItem item : itemsToProcess) {
            // Check if packet already exists for this (course, targetCycle)
            if (packetRepository.existsByCourse_CourseIdAndAcademicCycle_CycleId(item.getCourseId(), targetCycle.getCycleId())) {
                continue; // Skip already created packet to prevent duplicates
            }

            Course course = courseRepository.findById(item.getCourseId()).orElse(null);
            if (course == null) continue;

            CourseStaffOverride override = overrideMap.get(item.getCourseId());

            Long lecturerId = override != null && override.getLecturerId() != null
                    ? override.getLecturerId()
                    : item.getLecturerId();

            Long moderatorId = override != null && override.getModeratorId() != null
                    ? override.getModeratorId()
                    : item.getModeratorId();

            LocalDate deadline = override != null && override.getCustomDeadline() != null
                    ? override.getCustomDeadline()
                    : item.getCalculatedDeadline();

            LocalDate modDeadline = override != null && override.getCustomModerationDeadline() != null
                    ? override.getCustomModerationDeadline()
                    : item.getCalculatedModerationDeadline();

            User lecturer = lecturerId != null ? userRepository.findById(lecturerId).orElse(null) : null;
            User moderator = moderatorId != null ? userRepository.findById(moderatorId).orElse(null) : null;

            ExamPacket packet = ExamPacket.builder()
                    .academicCycle(targetCycle)
                    .course(course)
                    .lecturer(lecturer)
                    .moderator(moderator)
                    .status(pendingStatus)
                    .deadline(deadline)
                    .moderationDeadline(modDeadline)
                    .duration(item.getDefaultDuration())
                    .totalMarks(item.getDefaultTotalMarks())
                    .questions("All sections mandatory")
                    .format("Standard Exam")
                    .build();

            ExamPacket savedPacket = packetRepository.save(packet);
            createdCount++;

            if (lecturer != null) assignedLecturers++;
            if (moderator != null) assignedModerators++;

            String creatorName = username != null ? username : "Academic Registry";
            activityLogService.logForPacket(
                    savedPacket,
                    pendingStatus != null ? pendingStatus.getStatusName() : "PENDING",
                    "Exam packet rolled over for " + targetCycleName + " [Course: " + course.getCourseCode() + "]",
                    creatorName,
                    "AR",
                    "bg-purple-500"
            );

            // Notify staff
            if (lecturer != null) {
                notificationService.createNotification(
                        lecturer,
                        savedPacket,
                        "New Semester Assignment",
                        "You have been assigned as the Lecturer for " + course.getCourseCode() + " (" + course.getCourseName() + ") in " + targetCycleName + ".",
                        "COURSE",
                        false
                );
            }
            if (moderator != null) {
                notificationService.createNotification(
                        moderator,
                        savedPacket,
                        "New Semester Moderation Assignment",
                        "You have been assigned as Moderator for " + course.getCourseCode() + " (" + course.getCourseName() + ") in " + targetCycleName + ".",
                        "COURSE",
                        false
                );
            }
        }

        return RolloverExecutionResult.builder()
                .cycleId(targetCycle.getCycleId())
                .cycleName(targetCycle.getCycleName())
                .status(targetCycle.getStatus())
                .createdPacketsCount(createdCount)
                .assignedLecturersCount(assignedLecturers)
                .assignedModeratorsCount(assignedModerators)
                .message("Successfully rolled over " + createdCount + " courses into " + targetCycleName + ".")
                .build();
    }
}
