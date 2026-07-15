package com.exam_paper.backend.repository;


import com.exam_paper.backend.dto.PacketMovementResponseDTO;
import com.exam_paper.backend.entity.PacketMovement;

import org.springframework.data.jpa.repository.*;

import org.springframework.data.repository.query.Param;


import java.util.List;



public interface PacketMovementRepository
        extends JpaRepository<PacketMovement,Long>{



    @Query("""
            SELECT new com.exam_paper.backend.dto.PacketMovementResponseDTO(

            fromUser.fullName,
            toUser.fullName,
            pm.action,
            pm.timestamp

            )

            FROM PacketMovement pm

            JOIN pm.fromUser fromUser

            JOIN pm.toUser toUser

            WHERE pm.packet.packetId=:packetId

            ORDER BY pm.timestamp DESC

            """)
    List<PacketMovementResponseDTO> getPacketHistory(
            @Param("packetId") Long packetId
    );

}