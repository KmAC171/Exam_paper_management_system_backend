package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.LecturerDashboardResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerDashboardService {

    private final PacketAssignmentRepository packetAssignmentRepository;


    /**
     * Task: Calculate and return dashboard statistics
     * for a lecturer.
     */
    public LecturerDashboardResponseDTO getDashboard(
            String lecturerId
    ) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository
                        .findByUserUserId(lecturerId);


        long totalActiveTasks = 0;
        long completedTasks = 0;
        long overdueItems = 0;

        int totalScripts = 0;


        LocalDate today =
                LocalDate.now();

        LocalDate nextDeadline = null;


        for (PacketAssignment assignment : assignments) {

            ExamPacket packet =
                    assignment.getPacket();


            // Ignore assignments without a packet
            if (packet == null) {
                continue;
            }


            // Count every packet assigned to this lecturer
            totalActiveTasks++;


            // Get script count via the packet's
            // associated Marking entity
            Marking marking =
                    packet.getMarking();


            if (marking != null &&
                    marking.getTotalScripts() != null) {

                totalScripts +=
                        marking.getTotalScripts();
            }


            String status =
                    packet.getStatus();


            // Completed packet
            if ("Completed".equalsIgnoreCase(status)) {

                completedTasks++;
            }


            // Check overdue
            if (
                    packet.getDeadline() != null &&
                            packet.getDeadline().isBefore(today) &&
                            !"Completed".equalsIgnoreCase(status)
            ) {

                overdueItems++;
            }


            // Find nearest upcoming deadline
            if (
                    packet.getDeadline() != null &&
                            !packet.getDeadline().isBefore(today) &&
                            !"Completed".equalsIgnoreCase(status)
            ) {

                if (
                        nextDeadline == null ||
                                packet.getDeadline()
                                        .isBefore(nextDeadline)
                ) {

                    nextDeadline =
                            packet.getDeadline();
                }
            }
        }


        return new LecturerDashboardResponseDTO(

                lecturerId,

                totalActiveTasks,

                completedTasks,

                overdueItems,

                totalScripts,

                nextDeadline

        );
    }
}