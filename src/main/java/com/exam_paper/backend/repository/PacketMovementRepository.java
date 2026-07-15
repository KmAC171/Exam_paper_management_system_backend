package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.PacketMovement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface PacketMovementRepository
        extends JpaRepository<PacketMovement, Long> {



    List<PacketMovement>
    findByPacketPacketIdOrderByTimestampDesc(
            Long packetId
    );

}