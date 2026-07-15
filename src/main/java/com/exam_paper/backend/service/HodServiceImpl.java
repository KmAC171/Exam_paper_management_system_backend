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



    @Override
    public List<HodPacketResponseDTO> getAllPackets() {


        return examPacketRepository.findAllPackets();

    }





    @Override
    public PacketDetailsResponseDTO getPacketDetails(Long packetId) {


        return examPacketRepository
                .getPacketDetails(packetId);

    }





    @Override
    public List<PacketMovementResponseDTO> getPacketHistory(
            Long packetId
    ) {


        return packetMovementRepository
                .getPacketHistory(packetId);

    }





    @Override
    public List<HodPacketResponseDTO> searchPackets(
            String keyword
    ) {


        return examPacketRepository
                .searchPackets(keyword)
                .stream()

                .map(this::convertToDTO)

                .toList();

    }





    @Override
    public List<HodPacketResponseDTO> filterPackets(
            String status
    ) {


        return examPacketRepository
                .findByStatus(status)

                .stream()

                .map(this::convertToDTO)

                .toList();

    }





    @Override
    public List<WorkloadResponseDTO> getStaffWorkload() {


        return packetAssignmentRepository
                .getStaffWorkload();

    }





    @Override
    public List<MarkingProgressResponseDTO> getMarkingProgress() {


        return markingRepository
                .getMarkingProgress();

    }





    @Override
    public Comment addComment(
            Comment comment
    ) {


        return commentRepository.save(comment);

    }





    @Override
    public List<Comment> getComments(
            Long packetId
    ) {


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

}