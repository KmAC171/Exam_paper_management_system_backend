package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.PrintingSchedule;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.PrintingScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerTaskService {

    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final MarkingRepository markingRepository;
    private final PrintingScheduleRepository printingScheduleRepository;


    // =========================================================
    // UPDATE PACKET STATUS
    // =========================================================

    public String updatePacketStatus(
            String packetId,
            UpdatePacketStatusRequestDTO request
    ) {

        ExamPacket packet = examPacketRepository
                .findById(packetId)
                .orElseThrow(
                        () -> new RuntimeException("Packet not found")
                );

        packet.setStatus(request.getStatus());

        examPacketRepository.save(packet);

        return "Packet status updated successfully";
    }


    // =========================================================
    // COMPLETE TASK
    // =========================================================

    public CompleteTaskResponseDTO completeTask(String packetId) {

        ExamPacket packet = examPacketRepository
                .findById(packetId)
                .orElseThrow(
                        () -> new RuntimeException("Packet not found")
                );

        packet.setStatus("COMPLETED");

        examPacketRepository.save(packet);

        return CompleteTaskResponseDTO.builder()
                .packetId(packet.getPacketId())
                .status(packet.getStatus())
                .message("Task marked as completed successfully")
                .build();
    }


    // =========================================================
    // TASK SUMMARY
    // =========================================================

    public LecturerTaskSummaryResponseDTO getTaskSummary(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        long pending = 0;
        long completed = 0;
        long overdue = 0;

        LocalDate today = LocalDate.now();

        for (PacketAssignment assignment : assignments) {

            if (assignment == null ||
                    assignment.getPacket() == null) {
                continue;
            }

            ExamPacket packet = assignment.getPacket();

            String status = packet.getStatus();

            LocalDate deadline = packet.getDeadline();

            if ("COMPLETED".equalsIgnoreCase(status)) {

                completed++;

            } else if (
                    deadline != null &&
                            deadline.isBefore(today)
            ) {

                overdue++;

            } else {

                pending++;
            }
        }

        return new LecturerTaskSummaryResponseDTO(
                lecturerId,
                pending,
                completed,
                overdue
        );
    }


    // =========================================================
    // WORKLOAD STATISTICS
    // =========================================================

    public LecturerWorkloadStatisticsDTO getWorkloadStatistics(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<Marking> markings =
                markingRepository
                        .findByLecturerUserId(lecturerId);

        long totalAssignedPackets = assignments.size();

        long completedPackets = 0;
        long pendingPackets = 0;
        long overduePackets = 0;

        LocalDate today = LocalDate.now();

        for (PacketAssignment assignment : assignments) {

            if (assignment == null ||
                    assignment.getPacket() == null) {
                continue;
            }

            ExamPacket packet = assignment.getPacket();

            String status = packet.getStatus();

            if ("COMPLETED".equalsIgnoreCase(status)) {

                completedPackets++;

            } else if (
                    packet.getDeadline() != null &&
                            packet.getDeadline().isBefore(today)
            ) {

                overduePackets++;

            } else {

                pendingPackets++;
            }
        }


        // =====================================================
        // SCRIPT COUNTS
        // =====================================================

        int totalScripts = 0;

        if (markings != null) {

            for (Marking marking : markings) {

                if (marking != null &&
                        marking.getTotalScripts() != null) {

                    totalScripts += marking.getTotalScripts();
                }
            }
        }


        /*
         * At the moment your Marking entity does not contain a
         * markedScripts value, so this remains zero.
         */
        int markedScripts = 0;

        int remainingScripts =
                Math.max(0, totalScripts - markedScripts);


        return new LecturerWorkloadStatisticsDTO(
                lecturerId,
                totalAssignedPackets,
                totalScripts,
                markedScripts,
                remainingScripts,
                completedPackets,
                pendingPackets,
                overduePackets
        );
    }


    // =========================================================
    // DEADLINE CALENDAR
    // =========================================================

    public List<LecturerDeadlineCalendarDTO> getDeadlineCalendar(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<LecturerDeadlineCalendarDTO> deadlines =
                new ArrayList<>();

        for (PacketAssignment assignment : assignments) {

            if (assignment == null ||
                    assignment.getPacket() == null) {
                continue;
            }

            ExamPacket packet = assignment.getPacket();

            Course course = packet.getCourse();

            if (course == null) {
                continue;
            }

            deadlines.add(
                    new LecturerDeadlineCalendarDTO(
                            packet.getPacketId(),
                            course.getCourseCode(),
                            course.getCourseName(),
                            packet.getDeadline(),
                            packet.getStatus()
                    )
            );
        }

        return deadlines;
    }


    // =========================================================
    // PRINTING SCHEDULES
    // =========================================================

    public List<LecturerPrintingScheduleDTO> getPrintingSchedules(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<LecturerPrintingScheduleDTO> response =
                new ArrayList<>();

        for (PacketAssignment assignment : assignments) {

            if (assignment == null ||
                    assignment.getPacket() == null) {
                continue;
            }

            ExamPacket packet = assignment.getPacket();

            if (packet.getCourse() == null) {
                continue;
            }

            List<PrintingSchedule> schedules =
                    printingScheduleRepository
                            .findByPacketPacketId(
                                    packet.getPacketId()
                            );

            if (schedules == null) {
                continue;
            }

            for (PrintingSchedule schedule : schedules) {

                if (schedule == null) {
                    continue;
                }

                response.add(
                        new LecturerPrintingScheduleDTO(
                                packet.getPacketId(),
                                packet.getCourse().getCourseCode(),
                                packet.getCourse().getCourseName(),
                                schedule.getStatus(),
                                packet.getDeadline()
                        )
                );
            }
        }

        return response;
    }


    // =========================================================
    // PREVIOUS / ARCHIVED PACKETS
    // =========================================================

    public List<PreviousPacketResponseDTO> getPreviousPackets() {

        List<ExamPacket> packets =
                examPacketRepository.findByStatus("COMPLETED");

        List<PreviousPacketResponseDTO> response =
                new ArrayList<>();

        for (ExamPacket packet : packets) {

            if (packet == null) {
                continue;
            }

            response.add(
                    convertPreviousPacketToDTO(packet)
            );
        }

        return response;
    }


    // =========================================================
    // PREVIOUS PACKET DTO CONVERTER
    // =========================================================

    private PreviousPacketResponseDTO convertPreviousPacketToDTO(
            ExamPacket packet
    ) {

        String courseCode = null;
        String courseName = null;
        String departmentName = null;

        if (packet.getCourse() != null) {

            courseCode =
                    packet.getCourse().getCourseCode();

            courseName =
                    packet.getCourse().getCourseName();

            if (packet.getCourse().getDepartment() != null) {

                departmentName =
                        packet.getCourse()
                                .getDepartment()
                                .getDeptName();
            }
        }


        Integer academicYear = null;
        Integer semester = null;

        if (packet.getAcademicCycle() != null) {

            academicYear =
                    packet.getAcademicCycle().getYear();

            semester =
                    packet.getAcademicCycle().getSemester();
        }


        String currentHolderName = null;

        if (packet.getCurrentHolder() != null) {

            currentHolderName =
                    packet.getCurrentHolder().getName();
        }


        return PreviousPacketResponseDTO.builder()

                .packetId(
                        packet.getPacketId()
                )

                .courseCode(
                        courseCode
                )

                .courseName(
                        courseName
                )

                .departmentName(
                        departmentName
                )

                .academicYear(
                        academicYear
                )

                .semester(
                        semester
                )

                .status(
                        packet.getStatus()
                )

                .deadline(
                        packet.getDeadline()
                )

                .currentHolderName(
                        currentHolderName
                )

                .build();
    }
}