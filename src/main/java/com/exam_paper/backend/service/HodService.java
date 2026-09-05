package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HodService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final PacketRepository packetRepository;
    private final PacketService packetService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final PacketCommentRepository packetCommentRepository;

    private Department resolveDepartment(String username, Long deptIdOptional) {
        if (deptIdOptional != null && deptIdOptional > 0) {
            return departmentRepository.findById(deptIdOptional).orElse(null);
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getDepartment() != null) {
            return user.getDepartment();
        }

        // Fallback to first department if any
        return departmentRepository.findAll().stream().findFirst().orElse(null);
    }

    @Transactional(readOnly = true)
    public HodDashboardDTO getDepartmentDashboard(String username, Long deptIdOptional) {
        packetService.syncMissingPacketsForCourses();

        Department dept = resolveDepartment(username, deptIdOptional);
        if (dept == null) {
            return HodDashboardDTO.builder()
                    .departmentName("Department Not Assigned")
                    .departmentCode("N/A")
                    .faculty("N/A")
                    .recentPackets(List.of())
                    .recentActivities(List.of())
                    .build();
        }

        List<ExamPacket> packets = packetRepository.findByDepartmentIdWithDetails(dept.getDepartmentId());
        LocalDate today = LocalDate.now();

        long totalPackets = packets.size();
        long pending = 0;
        long draft = 0;
        long submitted = 0;
        long approved = 0;
        long printing = 0;
        long completed = 0;
        long rejected = 0;
        long overdue = 0;

        for (ExamPacket p : packets) {
            String status = p.getStatus() != null ? p.getStatus().getStatusName().toUpperCase() : "PENDING";
            boolean isOverdue = p.getDeadline() != null && p.getDeadline().isBefore(today) && !status.equals("COMPLETED");

            if (isOverdue) {
                overdue++;
            }

            switch (status) {
                case "PENDING" -> pending++;
                case "DRAFT" -> draft++;
                case "SUBMITTED", "UNDER_MODERATION" -> submitted++;
                case "APPROVED" -> approved++;
                case "PRINTING", "PRINTING_QUEUE" -> printing++;
                case "COMPLETED" -> completed++;
                case "REJECTED" -> rejected++;
                default -> pending++;
            }
        }

        long totalCourses = courseRepository.countByDepartment_DepartmentId(dept.getDepartmentId());
        long totalStaff = userRepository.countByDepartment_DepartmentId(dept.getDepartmentId());

        List<PacketDTO> recentPackets = packets.stream()
                .sorted(Comparator.comparing(ExamPacket::getPacketId).reversed())
                .limit(8)
                .map(packetService::toDTO)
                .collect(Collectors.toList());

        List<ActivityLogDTO> recentActivities = activityLogService.getRecentActivity().stream()
                .limit(6)
                .map(log -> new ActivityLogDTO(
                        log.getMessage(),
                        log.getActorInitials(),
                        log.getActorColor(),
                        timeAgo(log.getCreatedAt())
                ))
                .collect(Collectors.toList());

        String deptCode = dept.getDepartmentName() != null && dept.getDepartmentName().length() >= 4
                ? dept.getDepartmentName().substring(0, 4).toUpperCase()
                : "DEPT";

        return HodDashboardDTO.builder()
                .departmentId(dept.getDepartmentId())
                .departmentName(dept.getDepartmentName())
                .departmentCode(deptCode)
                .faculty("Faculty of Science and Technology")
                .totalPackets(totalPackets)
                .pendingPackets(pending)
                .draftPackets(draft)
                .submittedPackets(submitted)
                .approvedPackets(approved)
                .printingPackets(printing)
                .completedPackets(completed)
                .rejectedPackets(rejected)
                .overduePackets(overdue)
                .totalCourses(totalCourses)
                .totalStaff(totalStaff)
                .recentPackets(recentPackets)
                .recentActivities(recentActivities)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PacketDTO> getDepartmentPackets(String username, Long deptIdOptional) {
        packetService.syncMissingPacketsForCourses();
        Department dept = resolveDepartment(username, deptIdOptional);
        if (dept == null) {
            return List.of();
        }

        List<ExamPacket> packets = packetRepository.findByDepartmentIdWithDetails(dept.getDepartmentId());
        return packets.stream()
                .map(packetService::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HodWorkloadDTO> getDepartmentWorkload(String username, Long deptIdOptional) {
        packetService.syncMissingPacketsForCourses();
        Department dept = resolveDepartment(username, deptIdOptional);
        if (dept == null) {
            return List.of();
        }

        List<User> deptUsers = userRepository.findByDepartment_DepartmentId(dept.getDepartmentId());
        List<ExamPacket> deptPackets = packetRepository.findByDepartmentIdWithDetails(dept.getDepartmentId());
        LocalDate today = LocalDate.now();

        // Also include any users who are assigned as lecturer or moderator in this department's packets
        Set<User> relevantStaff = new LinkedHashSet<>(deptUsers);
        for (ExamPacket p : deptPackets) {
            if (p.getLecturer() != null) relevantStaff.add(p.getLecturer());
            if (p.getModerator() != null) relevantStaff.add(p.getModerator());
        }

        List<HodWorkloadDTO> workloadList = new ArrayList<>();

        for (User staff : relevantStaff) {
            if (staff.getRole() == User.Role.ROLE_ADMIN) continue; // Skip AR

            List<ExamPacket> assignedAsLecturer = deptPackets.stream()
                    .filter(p -> p.getLecturer() != null && p.getLecturer().getUserId().equals(staff.getUserId()))
                    .collect(Collectors.toList());

            List<ExamPacket> assignedAsModerator = deptPackets.stream()
                    .filter(p -> p.getModerator() != null && p.getModerator().getUserId().equals(staff.getUserId()))
                    .collect(Collectors.toList());

            List<ExamPacket> allAssigned = new ArrayList<>(assignedAsLecturer);
            for (ExamPacket mp : assignedAsModerator) {
                if (!allAssigned.contains(mp)) {
                    allAssigned.add(mp);
                }
            }

            List<String> assignedCourses = allAssigned.stream()
                    .map(p -> p.getCourse() != null ? p.getCourse().getCourseCode() + " - " + p.getCourse().getCourseName() : "Unknown Course")
                    .distinct()
                    .collect(Collectors.toList());

            long pending = 0;
            long draft = 0;
            long submitted = 0;
            long approved = 0;
            long printing = 0;
            long completed = 0;
            long rejected = 0;
            long overdue = 0;

            for (ExamPacket p : allAssigned) {
                String status = p.getStatus() != null ? p.getStatus().getStatusName().toUpperCase() : "PENDING";
                boolean isOverdue = p.getDeadline() != null && p.getDeadline().isBefore(today) && !status.equals("COMPLETED");
                if (isOverdue) overdue++;

                switch (status) {
                    case "PENDING" -> pending++;
                    case "DRAFT" -> draft++;
                    case "SUBMITTED", "UNDER_MODERATION" -> submitted++;
                    case "APPROVED" -> approved++;
                    case "PRINTING", "PRINTING_QUEUE" -> printing++;
                    case "COMPLETED" -> completed++;
                    case "REJECTED" -> rejected++;
                    default -> pending++;
                }
            }

            int totalAssigned = allAssigned.size();
            int totalScripts = totalAssigned * 45; // Standard cohort script estimate
            int markedScripts = (int) (completed * 45 + approved * 35 + printing * 40 + submitted * 20 + draft * 10);
            int progressPercentage = totalAssigned > 0
                    ? (int) Math.round(((completed * 100.0) + (approved * 85.0) + (printing * 95.0) + (submitted * 50.0) + (draft * 25.0)) / (totalAssigned * 100.0) * 100)
                    : 0;

            if (progressPercentage > 100) progressPercentage = 100;

            workloadList.add(HodWorkloadDTO.builder()
                    .lecturerId(staff.getUserId())
                    .lecturerName(staff.getFullName())
                    .username(staff.getUsername())
                    .email(staff.getEmail())
                    .role(staff.getRole() != null ? staff.getRole().name() : "ROLE_USER")
                    .assignedCourses(assignedCourses)
                    .totalAssignedPackets(totalAssigned)
                    .pendingPackets(pending)
                    .draftPackets(draft)
                    .submittedPackets(submitted)
                    .approvedPackets(approved)
                    .printingPackets(printing)
                    .completedPackets(completed)
                    .rejectedPackets(rejected)
                    .overduePackets(overdue)
                    .totalScripts(totalScripts)
                    .markedScripts(markedScripts)
                    .progressPercentage(progressPercentage)
                    .build());
        }

        return workloadList;
    }

    @Transactional(readOnly = true)
    public List<PacketDTO> getDepartmentOverdue(String username, Long deptIdOptional) {
        packetService.syncMissingPacketsForCourses();
        Department dept = resolveDepartment(username, deptIdOptional);
        if (dept == null) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        List<ExamPacket> packets = packetRepository.findByDepartmentIdWithDetails(dept.getDepartmentId());

        return packets.stream()
                .filter(p -> {
                    String status = p.getStatus() != null ? p.getStatus().getStatusName().toUpperCase() : "PENDING";
                    return p.getDeadline() != null && p.getDeadline().isBefore(today) && !status.equals("COMPLETED");
                })
                .map(packetService::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PacketDTO> getDepartmentPreviousRecords(String username, Long deptIdOptional) {
        packetService.syncMissingPacketsForCourses();
        Department dept = resolveDepartment(username, deptIdOptional);
        if (dept == null) {
            return List.of();
        }

        List<ExamPacket> packets = packetRepository.findByDepartmentIdWithDetails(dept.getDepartmentId());

        return packets.stream()
                .filter(p -> {
                    String status = p.getStatus() != null ? p.getStatus().getStatusName().toUpperCase() : "PENDING";
                    return status.equals("COMPLETED");
                })
                .map(packetService::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HodReportDTO getDepartmentReport(String username, Long deptIdOptional) {
        packetService.syncMissingPacketsForCourses();
        Department dept = resolveDepartment(username, deptIdOptional);
        if (dept == null) {
            return HodReportDTO.builder()
                    .departmentName("N/A")
                    .courseBreakdown(List.of())
                    .build();
        }

        List<ExamPacket> packets = packetRepository.findByDepartmentIdWithDetails(dept.getDepartmentId());
        LocalDate today = LocalDate.now();

        long total = packets.size();
        long completed = 0;
        long inProgress = 0;
        long overdue = 0;

        List<HodReportDTO.CourseBreakdownDTO> courseBreakdowns = new ArrayList<>();

        for (ExamPacket p : packets) {
            String status = p.getStatus() != null ? p.getStatus().getStatusName().toUpperCase() : "PENDING";
            boolean isOverdue = p.getDeadline() != null && p.getDeadline().isBefore(today) && !status.equals("COMPLETED");

            if (isOverdue) overdue++;

            if (status.equals("COMPLETED")) {
                completed++;
            } else {
                inProgress++;
            }

            courseBreakdowns.add(HodReportDTO.CourseBreakdownDTO.builder()
                    .courseId(p.getCourse() != null ? p.getCourse().getCourseId() : null)
                    .courseCode(p.getCourse() != null ? p.getCourse().getCourseCode() : "N/A")
                    .courseName(p.getCourse() != null ? p.getCourse().getCourseName() : "N/A")
                    .semester("Semester 1")
                    .academicYear("2025/2026")
                    .lecturerName(p.getLecturer() != null ? p.getLecturer().getFullName() : "Unassigned")
                    .moderatorName(p.getModerator() != null ? p.getModerator().getFullName() : "Unassigned")
                    .status(status)
                    .deadline(p.getDeadline() != null ? p.getDeadline().toString() : "N/A")
                    .overdue(isOverdue)
                    .build());
        }

        int completionRate = total > 0 ? (int) Math.round((completed * 100.0) / total) : 0;

        String deptCode = dept.getDepartmentName() != null && dept.getDepartmentName().length() >= 4
                ? dept.getDepartmentName().substring(0, 4).toUpperCase()
                : "DEPT";

        return HodReportDTO.builder()
                .departmentId(dept.getDepartmentId())
                .departmentName(dept.getDepartmentName())
                .departmentCode(deptCode)
                .faculty("Faculty of Science and Technology")
                .totalPackets(total)
                .completedPackets(completed)
                .inProgressPackets(inProgress)
                .overduePackets(overdue)
                .completionPercentage(completionRate)
                .courseBreakdown(courseBreakdowns)
                .build();
    }

    @Transactional
    public void notifyStaff(String hodUsername, HodNotifyStaffDTO dto) {
        User hod = userRepository.findByUsername(hodUsername)
                .orElseThrow(() -> new RuntimeException("HOD user not found: " + hodUsername));

        User target = userRepository.findById(dto.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target staff member not found with ID: " + dto.getTargetUserId()));

        ExamPacket packet = null;
        if (dto.getPacketId() != null) {
            packet = packetRepository.findById(dto.getPacketId()).orElse(null);
        }

        String title = dto.getTitle() != null && !dto.getTitle().isBlank()
                ? dto.getTitle()
                : "Department Notice from " + hod.getFullName() + " (HOD)";

        String message = dto.getMessage();
        if (dto.getCourseCode() != null && !dto.getCourseCode().isBlank()) {
            message = "[" + dto.getCourseCode() + "] " + message;
        }

        notificationService.createNotification(
                target,
                packet,
                title,
                message,
                dto.isUrgent() ? "URGENT" : "HOD_NOTICE",
                dto.isUrgent()
        );
    }

    private String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "recently";
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + " hr ago";
        long days = ChronoUnit.DAYS.between(dateTime, now);
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }
}
