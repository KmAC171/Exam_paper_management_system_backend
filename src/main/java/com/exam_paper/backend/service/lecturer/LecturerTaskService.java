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


    public String updatePacketStatus(
            String packetId,
            UpdatePacketStatusRequestDTO request
    ) {

        ExamPacket packet =
                examPacketRepository
                        .findById(packetId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Packet not found"
                                )
                        );


        packet.setStatus(
                request.getStatus()
        );


        examPacketRepository.save(packet);


        return "Packet status updated successfully";
    }


    public CompleteTaskResponseDTO completeTask(
            String packetId
    ) {

        ExamPacket packet =
                examPacketRepository
                        .findById(packetId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Packet not found"
                                )
                        );


        // update status
        packet.setStatus("Completed");


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

            String status =
                    assignment.getPacket().getStatus();

            LocalDate deadline =
                    assignment.getPacket().getDeadline();


            if ("Completed".equalsIgnoreCase(status)) {

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


    /**
     * Retrieve workload statistics for the lecturer
     * without markedScripts tracking.
     */
    public LecturerWorkloadStatisticsDTO getWorkloadStatistics(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<Marking> markings =
                markingRepository
                        .findByLecturerUserId(lecturerId);


        long totalAssignedPackets =
                assignments.size();

        long completedPackets = 0;
        long pendingPackets = 0;
        long overduePackets = 0;

        LocalDate today = LocalDate.now();


        for (PacketAssignment assignment : assignments) {

            ExamPacket packet =
                    assignment.getPacket();


            if ("Completed".equalsIgnoreCase(
                    packet.getStatus()
            )) {

                completedPackets++;

            } else if (
                    packet.getDeadline() != null
                            && packet.getDeadline()
                            .isBefore(today)
            ) {

                overduePackets++;

            } else {

                pendingPackets++;
            }
        }


        int totalScripts = 0;


        for (Marking marking : markings) {

            totalScripts +=
                    marking.getTotalScripts();
        }


        int markedScripts = 0;

        int remainingScripts =
                totalScripts - markedScripts;


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


    public List<LecturerDeadlineCalendarDTO> getDeadlineCalendar(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<LecturerDeadlineCalendarDTO> deadlines =
                new ArrayList<>();


        for (PacketAssignment assignment : assignments) {

            ExamPacket packet =
                    assignment.getPacket();

            Course course =
                    packet.getCourse();


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


    public List<LecturerPrintingScheduleDTO> getPrintingSchedules(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);

        List<LecturerPrintingScheduleDTO> response =
                new ArrayList<>();


        for (PacketAssignment assignment : assignments) {

            ExamPacket packet =
                    assignment.getPacket();


            List<PrintingSchedule> schedules =
                    printingScheduleRepository
                            .findByPacketPacketId(
                                    packet.getPacketId()
                            );


            for (PrintingSchedule schedule : schedules) {

                response.add(

                        new LecturerPrintingScheduleDTO(

                                packet.getPacketId(),

                                packet.getCourse()
                                        .getCourseCode(),

                                packet.getCourse()
                                        .getCourseName(),

                                schedule.getStatus(),

                                packet.getDeadline()

                        )
                );
            }
        }


        return response;
    }
}