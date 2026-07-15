package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.Comment;
import com.exam_paper.backend.entity.ExamPacket;

import com.exam_paper.backend.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


import java.util.List;



@Service
@RequiredArgsConstructor
public class HodServiceImpl implements HodService {



    private final ExamPacketRepository examPacketRepository;

    private final PacketMovementRepository packetMovementRepository;

    private final PacketAssignmentRepository packetAssignmentRepository;

    private final CommentRepository commentRepository;

    private final MarkingRepository markingRepository;





    // View all packets

    @Override
    public List<HodPacketResponseDTO> getAllPackets() {


        return examPacketRepository
                .findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();

    }






    // View full packet details

    @Override
    public PacketDetailsResponseDTO getPacketDetails(Long packetId) {


        ExamPacket packet =
                examPacketRepository
                        .findById(packetId)
                        .orElseThrow(
                                () -> new RuntimeException("Packet not found")
                        );



        return new PacketDetailsResponseDTO(

                packet.getPacketId(),

                packet.getCourse()
                        .getCourseCode(),

                packet.getCourse()
                        .getCourseName(),

                packet.getCourse()
                        .getDepartment()
                        .getDepartmentName(),

                packet.getAcademicCycle()
                        .getYear(),

                packet.getAcademicCycle()
                        .getSemester(),

                packet.getStatus(),

                packet.getDeadline(),

                packet.getCurrentHolder()
                        .getFullName(),

                getAssignedLecturer(packet),

                getAssignedModerator(packet)

        );

    }








    // View movement history

    @Override
    public List<PacketMovementResponseDTO> getPacketHistory(Long packetId) {


        return packetMovementRepository
                .findByPacketPacketIdOrderByTimestampDesc(packetId)

                .stream()

                .map(m -> new PacketMovementResponseDTO(

                        m.getMovementId(),

                        m.getFromUser()
                                .getFullName(),

                        m.getToUser()
                                .getFullName(),

                        m.getAction(),

                        m.getTimestamp()

                ))

                .toList();

    }









    // Search packets

    @Override
    public List<HodPacketResponseDTO> searchPackets(
            String keyword
    ){


        return examPacketRepository
                .searchPackets(keyword)

                .stream()

                .map(this::convertToDTO)

                .toList();

    }









    // Filter by status

    @Override
    public List<HodPacketResponseDTO> filterPackets(
            String status
    ){


        return examPacketRepository
                .findByStatus(status)

                .stream()

                .map(this::convertToDTO)

                .toList();


    }









    // View packets assigned to lecturer

    @Override
    public List<HodPacketResponseDTO> getPacketsByLecturer(
            Long lecturerId
    ){


        return examPacketRepository
                .findPacketsAssignedToLecturer(lecturerId)

                .stream()

                .map(this::convertToDTO)

                .toList();


    }









    // Staff workload

    @Override
    public List<WorkloadResponseDTO> getStaffWorkload(){


        return packetAssignmentRepository
                .getStaffWorkload();


    }









    // Marking progress

    @Override
    public List<MarkingProgressResponseDTO> getMarkingProgress(){


        return markingRepository
                .getMarkingProgress();


    }









    // Add comment

    @Override
    public Comment addComment(Comment comment){


        return commentRepository.save(comment);


    }









    // Communication history

    @Override
    public List<Comment> getComments(
            Long packetId
    ){


        return commentRepository
                .findByPacketPacketIdOrderByTimestampDesc(packetId);


    }









    private HodPacketResponseDTO convertToDTO(
            ExamPacket packet
    ){


        return new HodPacketResponseDTO(

                packet.getPacketId(),

                packet.getCourse()
                        .getCourseCode(),

                packet.getCourse()
                        .getCourseName(),

                packet.getStatus(),

                packet.getDeadline(),

                packet.getCurrentHolder()
                        .getFullName(),

                packet.getAcademicCycle()
                        .getSemester(),

                packet.getAcademicCycle()
                        .getYear()

        );

    }









    private String getAssignedLecturer(
            ExamPacket packet
    ){


        return packet.getAssignments()

                .stream()

                .filter(a ->
                        a.getAssignedRole()
                                .equalsIgnoreCase("Lecturer")
                )

                .map(a ->
                        a.getUser()
                                .getFullName()
                )

                .findFirst()

                .orElse("Not Assigned");


    }









    private String getAssignedModerator(
            ExamPacket packet
    ){


        return packet.getAssignments()

                .stream()

                .filter(a ->
                        a.getAssignedRole()
                                .equalsIgnoreCase("Moderator")
                )

                .map(a ->
                        a.getUser()
                                .getFullName()
                )

                .findFirst()

                .orElse("Not Assigned");


    }


}