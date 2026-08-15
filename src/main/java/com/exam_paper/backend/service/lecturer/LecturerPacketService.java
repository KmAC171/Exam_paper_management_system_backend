package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketAssignment;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.entity.Course;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.PacketMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerPacketService {

    private final PacketAssignmentRepository packetAssignmentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final PacketMovementRepository packetMovementRepository;


    /*
        View exam packets assigned to lecturer
    */
    public List<AssignedPacketResponseDTO> getAssignedPackets(String lecturerId) {

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
    ) {

        ExamPacket packet = assignment.getPacket();

        if (packet == null) {
            throw new RuntimeException(
                    "Packet not found for assignment: "
                            + assignment.getAssignmentId()
            );
        }

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

                .taskType(
                        assignment.getTaskType()
                )

                .build();
    }


    public PacketDetailsResponseDTO getPacketDetails(String packetId) {

        ExamPacket packet =
                examPacketRepository.findByPacketId(packetId)
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


    /*
        Access previous academic packet records
    */
    public List<PreviousPacketResponseDTO> getPreviousPackets() {

        List<ExamPacket> packets =
                examPacketRepository
                        .findByAcademicCycleStatus("Completed");

        return packets.stream()
                .map(this::convertPreviousPacketToDTO)
                .collect(Collectors.toList());
    }


    private PreviousPacketResponseDTO convertPreviousPacketToDTO(
            ExamPacket packet
    ) {

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
    ) {

        List<PacketMovement> movements =
                packetMovementRepository
                        .findByPacketPacketId(packetId);

        return movements.stream()
                .map(this::convertMovementToDTO)
                .collect(Collectors.toList());
    }


    private PacketMovementResponseDTO convertMovementToDTO(
            PacketMovement movement
    ) {

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


    public List<ExamPacketResponseDTO> searchPackets(String keyword) {

        List<ExamPacket> packets =
                examPacketRepository.searchPackets(keyword);

        return packets.stream()
                .map(packet -> ExamPacketResponseDTO.builder()

                        .packetId(
                                packet.getPacketId()
                        )

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


    public LecturerPacketCountResponseDTO getAssignedPacketCount(
            String lecturerId
    ) {

        long count =
                packetAssignmentRepository
                        .countByUserUserId(lecturerId);

        return new LecturerPacketCountResponseDTO(
                lecturerId,
                count
        );
    }
}