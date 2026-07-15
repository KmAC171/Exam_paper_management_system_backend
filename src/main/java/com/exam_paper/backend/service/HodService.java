package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.Comment;

import java.util.List;


public interface HodService {


    // View all packets
    List<HodPacketResponseDTO> getAllPackets();



    // View packet details
    PacketDetailsResponseDTO getPacketDetails(Long packetId);



    // View packet movement history
    List<PacketMovementResponseDTO> getPacketHistory(Long packetId);



    // Search packets
    List<HodPacketResponseDTO> searchPackets(String keyword);



    // Filter packets by status
    List<HodPacketResponseDTO> filterPackets(String status);



    // View staff workload
    List<WorkloadResponseDTO> getStaffWorkload();



    // View marking progress
    List<MarkingProgressResponseDTO> getMarkingProgress();



    // Add comment
    Comment addComment(Comment comment);



    // Get comments
    List<Comment> getComments(Long packetId);

}