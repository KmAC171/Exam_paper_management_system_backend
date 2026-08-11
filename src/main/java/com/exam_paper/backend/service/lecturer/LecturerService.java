package com.exam_paper.backend.service.lecturer;


import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.repository.*;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class LecturerService {


    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final MarkingRepository markingRepository;
    private final UserRepository userRepository;
    private final PacketMovementRepository packetMovementRepository;
    private final CommentRepository commentRepository;
    private final PrintingScheduleRepository printingScheduleRepository;
    private final NotificationRepository notificationRepository;






    /*
        View exam packets assigned to lecturer
    */
    public List<AssignedPacketResponseDTO> getAssignedPackets(String lecturerId){


        List<PacketAssignment> assignments =
                packetAssignmentRepository.findByUserUserId(lecturerId);


        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

    }



    /*
        Convert Entity to DTO
    */
    private AssignedPacketResponseDTO convertToDto(
            PacketAssignment assignment
    ){

        ExamPacket packet = assignment.getPacket();


        return AssignedPacketResponseDTO.builder()


                .packetId(
                        packet.getPacketId()
                )


                .courseCode(
                        packet.getCourse()
                                .getCourseCode()
                )


                .courseName(
                        packet.getCourse()
                                .getCourseName()
                )


                .departmentName(
                        packet.getCourse()
                                .getDepartment()
                                .getDeptName()
                )


                .academicYear(
                        packet.getAcademicCycle()
                                .getYear()
                )


                .semester(
                        packet.getAcademicCycle()
                                .getSemester()
                )


                .deadline(
                        packet.getDeadline()
                )


                .status(
                        packet.getStatus()
                )


                .currentHolderName(
                        packet.getCurrentHolder()
                                .getName()
                )


                .build();

    }

    public PacketDetailsResponseDTO getPacketDetails(String packetId){

        ExamPacket packet = examPacketRepository.findByPacketId(packetId)
                .orElseThrow(() ->
                        new RuntimeException("Packet not found"));

        return PacketDetailsResponseDTO.builder()

                .packetId(packet.getPacketId())

                .courseCode(
                        packet.getCourse().getCourseCode()
                )

                .courseName(
                        packet.getCourse().getCourseName()
                )

                .departmentName(
                        packet.getCourse()
                                .getDepartment()
                                .getDeptName()
                )

                .academicYear(
                        packet.getAcademicCycle().getYear()
                )

                .semester(
                        packet.getAcademicCycle().getSemester()
                )

                .deadline(
                        packet.getDeadline()
                )

                .status(
                        packet.getStatus()
                )

                .currentHolderName(
                        packet.getCurrentHolder().getName()
                )

                .build();

    }

    public String addMarkingScripts(AddMarkingRequestDTO request){


        if(markingRepository.existsByPacketPacketId(
                request.getPacketId()
        )){

            throw new RuntimeException(
                    "Marking already added for this packet"
            );

        }



        Marking marking = Marking.builder()

                .markingId(
                        "MK"+System.currentTimeMillis()
                )

                .packet(
                        examPacketRepository
                                .findById(request.getPacketId())
                                .orElseThrow(
                                        () -> new RuntimeException(
                                                "Packet not found"
                                        )
                                )
                )


                .lecturer(
                        userRepository
                                .findById(request.getLecturerId())
                                .orElseThrow(
                                        () -> new RuntimeException(
                                                "Lecturer not found"
                                        )
                                )
                )


                .totalScripts(
                        request.getTotalScripts()
                )


                .markedScripts(0)


                .build();



        markingRepository.save(marking);



        return "Number of answer scripts added successfully";

    }

    /*
    Feature:
    Access previous academic packet records

    Previous records are packets
    where academic cycle status = Completed
*/
    public List<PreviousPacketResponseDTO> getPreviousPackets(){


        List<ExamPacket> packets =
                examPacketRepository
                        .findByAcademicCycleStatus("Completed");


        return packets.stream()
                .map(this::convertPreviousPacketToDTO)
                .collect(Collectors.toList());

    }




    private PreviousPacketResponseDTO convertPreviousPacketToDTO(
            ExamPacket packet
    ){


        return PreviousPacketResponseDTO.builder()


                .packetId(
                        packet.getPacketId()
                )


                .courseCode(
                        packet.getCourse()
                                .getCourseCode()
                )


                .courseName(
                        packet.getCourse()
                                .getCourseName()
                )


                .departmentName(
                        packet.getCourse()
                                .getDepartment()
                                .getDeptName()
                )


                .academicYear(
                        packet.getAcademicCycle()
                                .getYear()
                )


                .semester(
                        packet.getAcademicCycle()
                                .getSemester()
                )


                .status(
                        packet.getStatus()
                )


                .deadline(
                        packet.getDeadline()
                )


                .currentHolderName(
                        packet.getCurrentHolder()
                                .getName()
                )


                .build();

    }

    public List<PacketMovementResponseDTO> getPacketMovementHistory(
            String packetId
    ){


        List<PacketMovement> movements =
                packetMovementRepository
                        .findByPacketPacketId(packetId);



        return movements.stream()
                .map(this::convertMovementToDTO)
                .collect(Collectors.toList());

    }

    private PacketMovementResponseDTO convertMovementToDTO(
            PacketMovement movement
    ){


        return PacketMovementResponseDTO.builder()


                .movementId(
                        movement.getMovementId()
                )


                .fromUser(
                        movement.getFromUser()
                                .getName()
                )


                .toUser(
                        movement.getToUser()
                                .getName()
                )


                .action(
                        movement.getAction()
                )


                .timestamp(
                        movement.getTimestamp()
                )


                .build();

    }

    public String updatePacketStatus(
            String packetId,
            UpdatePacketStatusRequestDTO request
    ){


        ExamPacket packet =
                examPacketRepository.findById(packetId)
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
    ){


        ExamPacket packet =
                examPacketRepository.findById(packetId)
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
    public CommentResponseDTO addComment(
            String packetId,
            String userId,
            String text
    ){


        ExamPacket packet =
                examPacketRepository.findById(packetId)
                        .orElseThrow(
                                () -> new RuntimeException("Packet not found")
                        );


        User user =
                userRepository.findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException("User not found")
                        );


        Comment comment = Comment.builder()

                .commentId(
                        "CMT" + System.currentTimeMillis()
                )

                .packet(packet)

                .user(user)

                .commentText(text)

                .timestamp(LocalDateTime.now())

                .build();



        Comment saved =
                commentRepository.save(comment);



        return CommentResponseDTO.builder()

                .commentId(
                        saved.getCommentId()
                )

                .packetId(
                        saved.getPacket().getPacketId()
                )

                .userId(
                        saved.getUser().getUserId()
                )

                .userName(
                        saved.getUser().getName()
                )

                .commentText(
                        saved.getCommentText()
                )

                .timestamp(
                        saved.getTimestamp()
                )

                .build();

    }
    /*
    Feature:
    Add comments or feedback on packet

    Input:
    packet id
    user id
    comment text

    Output:
    Saved comment
*/

    public Comment addComment(AddCommentRequestDTO request){



        ExamPacket packet =
                examPacketRepository
                        .findById(request.getPacketId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Packet not found"
                                )
                        );



        User user =
                userRepository
                        .findById(request.getUserId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );



        Comment comment =
                Comment.builder()

                        .commentId(
                                generateCommentId()
                        )

                        .packet(packet)

                        .user(user)

                        .commentText(
                                request.getCommentText()
                        )

                        .timestamp(
                                LocalDateTime.now()
                        )

                        .build();



        return commentRepository.save(comment);

    }

    private String generateCommentId(){


        long count = commentRepository.count();


        return "CMT" + (count + 1);

    }





    /*
    Feature:
    View comments from others

*/



    // View all comments of a packet
    public List<CommentResponseDTO> getPacketComments(String packetId){


        List<Comment> comments =
                commentRepository.findByPacketPacketId(packetId);



        return comments.stream()
                .map(comment -> CommentResponseDTO.builder()

                        .commentId(comment.getCommentId())

                        .commentText(
                                comment.getCommentText()
                        )

                        .timestamp(
                                comment.getTimestamp()
                        )

                        .packetId(
                                comment.getPacket().getPacketId()
                        )

                        .userId(
                                comment.getUser().getUserId()
                        )

                        .userName(
                                comment.getUser().getName()
                        )

                        .build()
                )
                .toList();

    }


    public List<ExamPacketResponseDTO> searchPackets(String keyword) {

        List<ExamPacket> packets =
                examPacketRepository.searchPackets(keyword);

        return packets.stream()
                .map(packet -> ExamPacketResponseDTO.builder()

                        .packetId(packet.getPacketId())

                        .courseCode(
                                packet.getCourse().getCourseCode()
                        )

                        .courseName(
                                packet.getCourse().getCourseName()
                        )

                        .status(
                                packet.getStatus()
                        )

                        .build())
                .toList();
    }

    public LecturerPacketCountResponseDTO getAssignedPacketCount(String lecturerId) {

        long count = packetAssignmentRepository.countByUserUserId(lecturerId);

        return new LecturerPacketCountResponseDTO(
                lecturerId,
                count
        );
    }
    public LecturerMarkingSummaryResponseDTO getMarkingSummary(String lecturerId) {

        List<Marking> markings = markingRepository.findByLecturerUserId(lecturerId);

        if (markings.isEmpty()) {
            throw new RuntimeException("No marking records found for lecturer: " + lecturerId);
        }

        int totalScripts = 0;
        int markedScripts = 0;

        for (Marking marking : markings) {
            totalScripts += marking.getTotalScripts();
            markedScripts += marking.getMarkedScripts();
        }

        int remainingScripts = totalScripts - markedScripts;

        return new LecturerMarkingSummaryResponseDTO(
                lecturerId,
                totalScripts,
                markedScripts,
                remainingScripts
        );
    }

    public LecturerTaskSummaryResponseDTO getTaskSummary(String lecturerId) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository.findByUserUserId(lecturerId);

        long pending = 0;
        long completed = 0;
        long overdue = 0;

        LocalDate today = LocalDate.now();

        for (PacketAssignment assignment : assignments) {

            String status = assignment.getPacket().getStatus();
            LocalDate deadline = assignment.getPacket().getDeadline();

            if ("Completed".equalsIgnoreCase(status)) {

                completed++;

            } else if (deadline != null && deadline.isBefore(today)) {

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

    public LecturerWorkloadStatisticsDTO getWorkloadStatistics(String lecturerId) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository.findByUserUserId(lecturerId);

        List<Marking> markings =
                markingRepository.findByLecturerUserId(lecturerId);

        long totalAssignedPackets = assignments.size();

        long completedPackets = 0;
        long pendingPackets = 0;
        long overduePackets = 0;

        LocalDate today = LocalDate.now();

        for (PacketAssignment assignment : assignments) {

            ExamPacket packet = assignment.getPacket();

            if ("Completed".equalsIgnoreCase(packet.getStatus())) {

                completedPackets++;

            } else if (packet.getDeadline() != null
                    && packet.getDeadline().isBefore(today)) {

                overduePackets++;

            } else {

                pendingPackets++;
            }
        }

        int totalScripts = 0;
        int markedScripts = 0;

        for (Marking marking : markings) {

            totalScripts += marking.getTotalScripts();
            markedScripts += marking.getMarkedScripts();
        }

        int remainingScripts = totalScripts - markedScripts;

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

    public List<LecturerDeadlineCalendarDTO> getDeadlineCalendar(String lecturerId) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository.findByUserUserId(lecturerId);

        List<LecturerDeadlineCalendarDTO> deadlines = new ArrayList<>();

        for (PacketAssignment assignment : assignments) {

            ExamPacket packet = assignment.getPacket();
            Course course = packet.getCourse();

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

    public List<LecturerPrintingScheduleDTO> getPrintingSchedules(String lecturerId) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository.findByUserUserId(lecturerId);

        List<LecturerPrintingScheduleDTO> response = new ArrayList<>();


        for (PacketAssignment assignment : assignments) {

            ExamPacket packet = assignment.getPacket();


            List<PrintingSchedule> schedules =
                    printingScheduleRepository
                            .findByPacketPacketId(packet.getPacketId());


            for (PrintingSchedule schedule : schedules) {

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

    public List<NotificationResponseDTO> getNotifications(String userId) {


        List<Notification> notifications =
                notificationRepository
                        .findByUserUserIdOrderByCreatedAtDesc(userId);



        List<NotificationResponseDTO> response =
                new ArrayList<>();



        for(Notification notification : notifications){


            response.add(
                    new NotificationResponseDTO(

                            notification.getNotificationId(),

                            notification.getMessage(),

                            notification.getType(),

                            notification.getStatus(),

                            notification.getCreatedAt()

                    )
            );

        }


        return response;

    }



    /**
     * Task: Calculate and return dashboard statistics for a lecturer.
     *
     * This includes active tasks, scripts remaining to be marked,
     * completed tasks, overdue tasks, total scripts, marked scripts,
     * and the next upcoming deadline.
     */
    public LecturerDashboardResponseDTO getDashboard(String lecturerId) {

        List<PacketAssignment> assignments =
                packetAssignmentRepository.findByUserUserId(lecturerId);

        List<Marking> markings =
                markingRepository.findByLecturerUserId(lecturerId);

        long totalActiveTasks = 0;
        long completedTasks = 0;
        long overdueItems = 0;

        int totalScripts = 0;
        int markedScripts = 0;

        LocalDate today = LocalDate.now();
        LocalDate nextDeadline = null;

        for (PacketAssignment assignment : assignments) {

            ExamPacket packet = assignment.getPacket();

            if (packet == null) {
                continue;
            }

            String status = packet.getStatus();

            // Completed task
            if ("Completed".equalsIgnoreCase(status)) {

                completedTasks++;

            } else {

                // Any non-completed task is an active task
                totalActiveTasks++;

                // Check whether the active task is overdue
                if (packet.getDeadline() != null &&
                        packet.getDeadline().isBefore(today)) {

                    overdueItems++;
                }

                // Find next upcoming deadline
                if (packet.getDeadline() != null &&
                        !packet.getDeadline().isBefore(today)) {

                    if (nextDeadline == null ||
                            packet.getDeadline().isBefore(nextDeadline)) {

                        nextDeadline = packet.getDeadline();
                    }
                }
            }
        }

        // Calculate script statistics
        for (Marking marking : markings) {

            totalScripts += marking.getTotalScripts();
            markedScripts += marking.getMarkedScripts();
        }

        int scriptsToMark = Math.max(0, totalScripts - markedScripts);

        return new LecturerDashboardResponseDTO(
                lecturerId,
                totalActiveTasks,
                scriptsToMark,
                completedTasks,
                overdueItems,
                totalScripts,
                markedScripts,
                nextDeadline
        );
    }
    public List<LecturerMarkingProcessDTO> getMarkingProcess(String lecturerId) {

        List<Marking> markings =
                markingRepository.findByLecturerUserId(lecturerId);

        List<LecturerMarkingProcessDTO> response =
                new ArrayList<>();

        for (Marking marking : markings) {

            int remainingScripts =
                    marking.getTotalScripts() - marking.getMarkedScripts();

            double progress = 0;

            if (marking.getTotalScripts() > 0) {

                progress = ((double) marking.getMarkedScripts()
                        / marking.getTotalScripts()) * 100;

            }

            response.add(

                    new LecturerMarkingProcessDTO(

                            marking.getMarkingId(),

                            marking.getPacket().getPacketId(),

                            marking.getPacket().getCourse().getCourseCode(),

                            marking.getPacket().getCourse().getCourseName(),

                            marking.getTotalScripts(),

                            marking.getMarkedScripts(),

                            remainingScripts,

                            progress,

                            marking.getDeadline()

                    )

            );

        }

        return response;

    }
}