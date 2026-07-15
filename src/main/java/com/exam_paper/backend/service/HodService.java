package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.*;

import com.exam_paper.backend.entity.Comment;


import java.util.List;



public interface HodService {


    // View all packets in department
    List<HodPacketResponseDTO> getDepartmentPackets(Long departmentId);



    // View full packet details
    PacketDetailsResponseDTO getPacketDetails(Long packetId);



    // View packet movement history
    List<PacketMovementResponseDTO> getPacketHistory(Long packetId);



    // Search packets
    List<HodPacketResponseDTO> searchPackets(
            Long departmentId,
            String keyword
    );



    // Filter packets by status
    List<HodPacketResponseDTO> filterPackets(
            Long departmentId,
            String status
    );



    // Staff workload
    List<WorkloadResponseDTO> getWorkload(
            Long departmentId
    );



    // Marking progress
    List<MarkingProgressResponseDTO> getMarkingProgress(
            Long departmentId
    );



    // Add comment
    Comment addComment(Comment comment);



    // View comments
    List<Comment> getComments(Long packetId);

}