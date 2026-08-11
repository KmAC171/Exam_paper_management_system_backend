package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HodService {

    private final ExamPacketRepository examPacketRepository;
    private final PacketMovementRepository packetMovementRepository;
    private final CommentRepository commentRepository;
    private final MarkingRepository markingRepository;
    private final UserRepository userRepository;
    private final PacketAssignmentRepository packetAssignmentRepository;

    // ==========================================
    // BASIC PACKET & WORKFLOW MONITORING
    // ==========================================

    public List<DepartmentPacketResponseDto> getAllDepartmentPackets(String deptId) {
        return examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getDepartment() != null
                        && deptId.equalsIgnoreCase(p.getCourse().getDepartment().getDeptId()))
                .map(this::mapToPacketResponseDto)
                .collect(Collectors.toList());
    }

    public List<DepartmentPacketResponseDto> filterAndSearchPackets(
            String deptId, String query, String status, String cycleId, String lecturerId) {

        List<ExamPacket> packets = examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getDepartment() != null
                        && deptId.equalsIgnoreCase(p.getCourse().getDepartment().getDeptId()))
                .collect(Collectors.toList());

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            packets = packets.stream()
                    .filter(p -> (p.getCourse().getCourseCode() != null && p.getCourse().getCourseCode().toLowerCase().contains(q)) ||
                            (p.getCourse().getCourseName() != null && p.getCourse().getCourseName().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            packets = packets.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (cycleId != null && !cycleId.isBlank()) {
            packets = packets.stream()
                    .filter(p -> p.getAcademicCycle() != null && p.getAcademicCycle().getCycleId().equalsIgnoreCase(cycleId))
                    .collect(Collectors.toList());
        }

        if (lecturerId != null && !lecturerId.isBlank()) {
            List<String> assignedPacketIds = packetAssignmentRepository.findAll().stream()
                    .filter(pa -> pa.getUser() != null && pa.getUser().getUserId().equalsIgnoreCase(lecturerId))
                    .filter(pa -> pa.getPacket() != null)
                    .map(pa -> pa.getPacket().getPacketId())
                    .collect(Collectors.toList());

            packets = packets.stream()
                    .filter(p -> assignedPacketIds.contains(p.getPacketId()))
                    .collect(Collectors.toList());
        }

        return packets.stream().map(this::mapToPacketResponseDto).collect(Collectors.toList());
    }

    public PacketDetailDto getPacketDetails(String packetId) {
        ExamPacket packet = examPacketRepository.findById(packetId)
                .orElseThrow(() -> new RuntimeException("Exam packet not found with ID: " + packetId));

        List<PacketMovement> movements = packetMovementRepository.findAll().stream()
                .filter(pm -> pm.getPacket() != null && packetId.equalsIgnoreCase(pm.getPacket().getPacketId()))
                .collect(Collectors.toList());

        String lastUpdatedUser = "N/A";
        if (!movements.isEmpty()) {
            PacketMovement lastMovement = movements.get(movements.size() - 1);
            if (lastMovement.getFromUser() != null) {
                lastUpdatedUser = lastMovement.getFromUser().getName();
            }
        }

        List<PacketMovementDto> movementDtos = movements.stream()
                .map(m -> PacketMovementDto.builder()
                        .movementId(m.getMovementId())
                        .fromUserName(m.getFromUser() != null ? m.getFromUser().getName() : "N/A")
                        .toUserName(m.getToUser() != null ? m.getToUser().getName() : "N/A")
                        .action(m.getAction())
                        .timestamp(m.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return PacketDetailDto.builder()
                .packetId(packet.getPacketId())
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .departmentName(packet.getCourse() != null && packet.getCourse().getDepartment() != null ?
                        packet.getCourse().getDepartment().getDeptName() : "N/A")
                .cycleId(packet.getAcademicCycle() != null ? packet.getAcademicCycle().getCycleId() : "N/A")
                .status(packet.getStatus())
                .deadline(packet.getDeadline())
                .currentHolderName(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getName() : "Unassigned")
                .lastUpdatedUser(lastUpdatedUser)
                .movementHistory(movementDtos)
                .build();
    }

    // ==========================================
    // PAST RECORDS & OVERDUE TRACKING
    // ==========================================

    public List<DepartmentPacketResponseDto> getPreviousCycleRecords(String deptId) {
        return examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getDepartment() != null &&
                        deptId.equalsIgnoreCase(p.getCourse().getDepartment().getDeptId()))
                .filter(p -> p.getAcademicCycle() != null &&
                        "Completed".equalsIgnoreCase(p.getAcademicCycle().getStatus()))
                .map(this::mapToPacketResponseDto)
                .collect(Collectors.toList());
    }

    public List<DepartmentPacketResponseDto> getOverduePackets(String deptId) {
        return getAllDepartmentPackets(deptId).stream()
                .filter(DepartmentPacketResponseDto::isOverdue)
                .collect(Collectors.toList());
    }

    // ==========================================
    // WORKLOAD & MARKING PROGRESS
    // ==========================================

    public List<LecturerWorkloadDto> getDepartmentWorkload(String deptId) {
        List<User> departmentLecturers = userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null && deptId.equalsIgnoreCase(u.getDepartment().getDeptId()))
                .filter(u -> u.getRole() != null && "R1".equalsIgnoreCase(u.getRole().getRoleId()))
                .collect(Collectors.toList());

        return departmentLecturers.stream().map(lecturer -> {
            List<Marking> markings = markingRepository.findAll().stream()
                    .filter(m -> m.getLecturer() != null && lecturer.getUserId().equalsIgnoreCase(m.getLecturer().getUserId()))
                    .collect(Collectors.toList());

            long totalAssignedPackets = packetAssignmentRepository.findAll().stream()
                    .filter(pa -> pa.getUser() != null && lecturer.getUserId().equalsIgnoreCase(pa.getUser().getUserId()))
                    .count();

            long totalScripts = markings.stream().mapToLong(Marking::getTotalScripts).sum();
            long markedScripts = markings.stream().mapToLong(Marking::getMarkedScripts).sum();
            double progress = totalScripts == 0 ? 0.0 : ((double) markedScripts / totalScripts) * 100;

            return LecturerWorkloadDto.builder()
                    .lecturerId(lecturer.getUserId())
                    .lecturerName(lecturer.getName())
                    .totalAssignedPackets(totalAssignedPackets)
                    .totalScripts(totalScripts)
                    .markedScripts(markedScripts)
                    .progressPercentage(Math.round(progress * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }

    // ==========================================
    // COMMENTS & FEEDBACK
    // ==========================================

    public CommentResponseDto addComment(CommentRequestDto dto) {
        ExamPacket packet = examPacketRepository.findById(dto.getPacketId())
                .orElseThrow(() -> new RuntimeException("Packet not found with ID: " + dto.getPacketId()));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        Comment comment = new Comment();
        comment.setCommentId("CMT" + UUID.randomUUID().toString().substring(0, 5));
        comment.setPacket(packet);
        comment.setUser(user);
        comment.setCommentText(dto.getCommentText());
        comment.setTimestamp(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        return CommentResponseDto.builder()
                .commentId(saved.getCommentId())
                .packetId(packet.getPacketId())
                .userName(user.getName())
                .commentText(saved.getCommentText())
                .timestamp(saved.getTimestamp())
                .build();
    }

    public List<CommentResponseDto> getPacketComments(String packetId) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getPacket() != null && packetId.equalsIgnoreCase(c.getPacket().getPacketId()))
                .map(c -> CommentResponseDto.builder()
                        .commentId(c.getCommentId())
                        .packetId(packetId)
                        .userName(c.getUser() != null ? c.getUser().getName() : "Unknown")
                        .commentText(c.getCommentText())
                        .timestamp(c.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    // ==========================================
    // ANALYTICS & DOCUMENT EXPORTING
    // ==========================================

    public DepartmentReportDto generateDepartmentReport(String deptId) {
        List<DepartmentPacketResponseDto> packets = getAllDepartmentPackets(deptId);

        long total = packets.size();
        long pending = packets.stream().filter(p -> "Pending".equalsIgnoreCase(p.getStatus())).count();
        long inProgress = packets.stream().filter(p -> "In Progress".equalsIgnoreCase(p.getStatus())).count();
        long completed = packets.stream().filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count();
        long overdue = packets.stream().filter(DepartmentPacketResponseDto::isOverdue).count();

        List<LecturerWorkloadDto> workloads = getDepartmentWorkload(deptId);

        return DepartmentReportDto.builder()
                .departmentId(deptId)
                .departmentName(deptId)
                .totalPackets(total)
                .pendingPackets(pending)
                .inProgressPackets(inProgress)
                .completedPackets(completed)
                .overduePackets(overdue)
                .workloadDistribution(workloads)
                .build();
    }

    public byte[] exportDepartmentReportExcel(String deptId) {
        DepartmentReportDto report = generateDepartmentReport(deptId);
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("DEPARTMENT PERFORMANCE AND PROGRESS REPORT\n");
        csvBuilder.append("Department ID,").append(report.getDepartmentId()).append("\n");
        csvBuilder.append("Total Packets,").append(report.getTotalPackets()).append("\n");
        csvBuilder.append("Pending,").append(report.getPendingPackets()).append("\n");
        csvBuilder.append("In Progress,").append(report.getInProgressPackets()).append("\n");
        csvBuilder.append("Completed,").append(report.getCompletedPackets()).append("\n");
        csvBuilder.append("Overdue,").append(report.getOverduePackets()).append("\n\n");

        csvBuilder.append("LECTURER WORKLOAD & MARKING PROGRESS\n");
        csvBuilder.append("Lecturer ID,Lecturer Name,Assigned Packets,Total Scripts,Marked Scripts,Progress %\n");

        for (LecturerWorkloadDto w : report.getWorkloadDistribution()) {
            csvBuilder.append(String.format("%s,\"%s\",%d,%d,%d,%.2f%%\n",
                    w.getLecturerId(), w.getLecturerName(), w.getTotalAssignedPackets(),
                    w.getTotalScripts(), w.getMarkedScripts(), w.getProgressPercentage()));
        }
        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportDepartmentReportPdf(String deptId) {
        DepartmentReportDto report = generateDepartmentReport(deptId);
        StringBuilder pdfText = new StringBuilder();

        pdfText.append("=====================================================\n");
        pdfText.append("          DEPARTMENT ACADEMIC REPORT                 \n");
        pdfText.append("=====================================================\n\n");
        pdfText.append("Department ID: ").append(report.getDepartmentId()).append("\n");
        pdfText.append("Generated At: ").append(LocalDateTime.now()).append("\n\n");
        pdfText.append("SUMMARY STATISTICS:\n");
        pdfText.append("-----------------------------------------------------\n");
        pdfText.append("Total Exam Packets : ").append(report.getTotalPackets()).append("\n");
        pdfText.append("Pending Packets     : ").append(report.getPendingPackets()).append("\n");
        pdfText.append("In Progress Packets : ").append(report.getInProgressPackets()).append("\n");
        pdfText.append("Completed Packets   : ").append(report.getCompletedPackets()).append("\n");
        pdfText.append("Overdue Packets     : ").append(report.getOverduePackets()).append("\n\n");
        pdfText.append("LECTURER MARKING PROGRESS:\n");
        pdfText.append("-----------------------------------------------------\n");

        for (LecturerWorkloadDto w : report.getWorkloadDistribution()) {
            pdfText.append(String.format("- %s (%s): %d/%d Scripts Marked (%.1f%% Progress)\n",
                    w.getLecturerName(), w.getLecturerId(), w.getMarkedScripts(), w.getTotalScripts(), w.getProgressPercentage()));
        }

        return pdfText.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private DepartmentPacketResponseDto mapToPacketResponseDto(ExamPacket packet) {
        boolean isOverdue = packet.getDeadline() != null &&
                packet.getDeadline().isBefore(LocalDate.now()) &&
                !"Completed".equalsIgnoreCase(packet.getStatus());

        return DepartmentPacketResponseDto.builder()
                .packetId(packet.getPacketId())
                .courseCode(packet.getCourse() != null ? packet.getCourse().getCourseCode() : "N/A")
                .courseName(packet.getCourse() != null ? packet.getCourse().getCourseName() : "N/A")
                .cycleId(packet.getAcademicCycle() != null ? packet.getAcademicCycle().getCycleId() : "N/A")
                .status(packet.getStatus())
                .deadline(packet.getDeadline())
                .currentHolderId(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getUserId() : null)
                .currentHolderName(packet.getCurrentHolder() != null ? packet.getCurrentHolder().getName() : "Unassigned")
                .isOverdue(isOverdue)
                .build();
    }
}