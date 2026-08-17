package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.PrintingSchedule;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.PrintingScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerTaskService {

    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final MarkingRepository markingRepository;
    private final PrintingScheduleRepository printingScheduleRepository;


    // ============================================================
    // UPDATE PACKET STATUS
    // ============================================================

    public String updatePacketStatus(
            String packetId,
            UpdatePacketStatusRequestDTO request
    ) {

        ExamPacket packet = examPacketRepository
                .findById(packetId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Packet not found: " + packetId
                        )
                );

        if (request == null || request.getStatus() == null) {
            throw new RuntimeException(
                    "Status is required"
            );
        }

        packet.setStatus(
                request.getStatus()
        );

        examPacketRepository.save(packet);

        return "Packet status updated successfully";
    }


    // ============================================================
    // COMPLETE TASK
    // ============================================================

    public CompleteTaskResponseDTO completeTask(
            String packetId
    ) {

        ExamPacket packet = examPacketRepository
                .findById(packetId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Packet not found: " + packetId
                        )
                );

        /*
         * Use one consistent status value.
         *
         * Your frontend/database should preferably use:
         * COMPLETED
         */
        packet.setStatus("COMPLETED");

        examPacketRepository.save(packet);

        return CompleteTaskResponseDTO.builder()

                .packetId(
                        packet.getPacketId()
                )

                .status(
                        packet.getStatus()
                )

                .message(
                        "Task marked as completed successfully"
                )

                .build();
    }


    // ============================================================
    // TASK SUMMARY
    // ============================================================

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

            /*
             * Prevent NullPointerException
             */
            if (assignment == null) {
                continue;
            }

            ExamPacket packet =
                    assignment.getPacket();

            if (packet == null) {
                continue;
            }


            String status =
                    packet.getStatus();

            LocalDate deadline =
                    packet.getDeadline();


            /*
             * COMPLETED
             */
            if ("COMPLETED".equalsIgnoreCase(status)) {

                completed++;

            }

            /*
             * OVERDUE
             */
            else if (
                    deadline != null &&
                            deadline.isBefore(today)
            ) {

                overdue++;

            }

            /*
             * PENDING
             */
            else {

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


    // ============================================================
    // WORKLOAD STATISTICS
    // ============================================================

    public LecturerWorkloadStatisticsDTO getWorkloadStatistics(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<Marking> markings =
                markingRepository
                        .findByLecturerUserId(lecturerId);


        long totalAssignedPackets = 0;

        long completedPackets = 0;

        long pendingPackets = 0;

        long overduePackets = 0;


        LocalDate today = LocalDate.now();


        /*
         * Count only assignments that actually
         * contain a packet.
         */
        for (PacketAssignment assignment : assignments) {

            if (assignment == null) {
                continue;
            }

            ExamPacket packet =
                    assignment.getPacket();

            if (packet == null) {
                continue;
            }


            totalAssignedPackets++;


            String status =
                    packet.getStatus();

            LocalDate deadline =
                    packet.getDeadline();


            /*
             * COMPLETED
             */
            if ("COMPLETED".equalsIgnoreCase(status)) {

                completedPackets++;

            }

            /*
             * OVERDUE
             */
            else if (
                    deadline != null &&
                            deadline.isBefore(today)
            ) {

                overduePackets++;

            }

            /*
             * PENDING
             */
            else {

                pendingPackets++;
            }
        }


        // ========================================================
        // TOTAL SCRIPTS
        // ========================================================

        int totalScripts = 0;


        if (markings != null) {

            for (Marking marking : markings) {

                if (marking == null) {
                    continue;
                }

                if (marking.getTotalScripts() != null) {

                    totalScripts +=
                            marking.getTotalScripts();
                }
            }
        }


        /*
         * At the moment markedScripts tracking
         * has not been implemented.
         */
        int markedScripts = 0;


        int remainingScripts =
                Math.max(
                        totalScripts - markedScripts,
                        0
                );


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


    // ============================================================
    // DEADLINE CALENDAR
    // ============================================================

    public List<LecturerDeadlineCalendarDTO> getDeadlineCalendar(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);


        List<LecturerDeadlineCalendarDTO> deadlines =
                new ArrayList<>();


        /*
         * Today's date.
         */
        LocalDate today =
                LocalDate.now();


        for (PacketAssignment assignment : assignments) {

            /*
             * Prevent null assignment.
             */
            if (assignment == null) {
                continue;
            }


            /*
             * Prevent null packet.
             */
            ExamPacket packet =
                    assignment.getPacket();

            if (packet == null) {
                continue;
            }


            /*
             * Packet must have a deadline.
             */
            if (packet.getDeadline() == null) {
                continue;
            }


            /*
             * Packet must have a course.
             */
            Course course =
                    packet.getCourse();

            if (course == null) {
                continue;
            }


            /*
             * Only show upcoming deadlines.
             *
             * Example:
             *
             * Today = 2026-08-16
             *
             * 2026-08-10 -> ignored
             * 2026-08-15 -> ignored
             * 2026-08-16 -> included
             * 2026-08-20 -> included
             */
            if (packet.getDeadline().isBefore(today)) {
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


        /*
         * Sort by nearest deadline first.
         */
        deadlines.sort(
                Comparator.comparing(
                        LecturerDeadlineCalendarDTO::getDeadline
                )
        );


        return deadlines;
    }


    // ============================================================
// PRINTING SCHEDULES
// ============================================================

    public List<LecturerPrintingScheduleDTO> getPrintingSchedules(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<LecturerPrintingScheduleDTO> response =
                new ArrayList<>();

        if (assignments == null || assignments.isEmpty()) {
            return response;
        }

        for (PacketAssignment assignment : assignments) {

            if (assignment == null) {
                continue;
            }

            ExamPacket packet = assignment.getPacket();

            if (packet == null) {
                continue;
            }

            Course course = packet.getCourse();

            if (course == null) {
                continue;
            }

            List<PrintingSchedule> schedules =
                    printingScheduleRepository
                            .findByPacketPacketId(
                                    packet.getPacketId()
                            );

            if (schedules == null || schedules.isEmpty()) {
                continue;
            }

            for (PrintingSchedule schedule : schedules) {

                if (schedule == null) {
                    continue;
                }

                response.add(
                        new LecturerPrintingScheduleDTO(
                                packet.getPacketId(),
                                course.getCourseCode(),
                                course.getCourseName(),
                                schedule.getStatus(),
                                packet.getDeadline()
                        )
                );
            }
        }

        return response;
    }
}