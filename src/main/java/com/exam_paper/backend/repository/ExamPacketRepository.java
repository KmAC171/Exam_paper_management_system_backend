package com.exam_paper.backend.repository;


import com.exam_paper.backend.dto.HodPacketResponseDTO;
import com.exam_paper.backend.dto.PacketDetailsResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ExamPacketRepository
        extends JpaRepository<ExamPacket, Long> {


    // View all department packets
    // (Removed department filtering because User entity has no Department relation)

    @Query("""
            SELECT new com.exam_paper.backend.dto.HodPacketResponseDTO(

            ep.packetId,
            c.courseCode,
            c.courseName,
            ep.status,
            ep.deadline,
            u.fullName,
            ac.semester,
            ac.year

            )

            FROM ExamPacket ep

            JOIN ep.course c

            JOIN ep.currentHolder u

            JOIN ep.academicCycle ac

            """)
    List<HodPacketResponseDTO> findAllPackets();



    // Search packets

    @Query("""
            SELECT ep

            FROM ExamPacket ep

            JOIN ep.course c

            WHERE LOWER(c.courseName)
            LIKE LOWER(CONCAT('%',:keyword,'%'))

            OR LOWER(ep.status)
            LIKE LOWER(CONCAT('%',:keyword,'%'))

            """)
    List<ExamPacket> searchPackets(
            @Param("keyword") String keyword
    );



    // Filter by status

    List<ExamPacket> findByStatus(String status);



    // Packet details

    @Query("""
            SELECT new com.exam_paper.backend.dto.PacketDetailsResponseDTO(

            ep.packetId,
            c.courseCode,
            c.courseName,
            d.departmentName,
            ac.year,
            ac.semester,
            ep.status,
            ep.deadline,
            holder.fullName,
            lecturer.fullName,
            moderator.fullName

            )

            FROM ExamPacket ep

            JOIN ep.course c

            JOIN c.department d

            JOIN ep.academicCycle ac

            JOIN ep.currentHolder holder


            LEFT JOIN PacketAssignment pa1
            ON pa1.packet.packetId = ep.packetId
            AND pa1.assignedRole='Lecturer'


            LEFT JOIN pa1.user lecturer


            LEFT JOIN PacketAssignment pa2
            ON pa2.packet.packetId = ep.packetId
            AND pa2.assignedRole='Moderator'


            LEFT JOIN pa2.user moderator


            WHERE ep.packetId=:packetId

            """)
    PacketDetailsResponseDTO getPacketDetails(
            @Param("packetId") Long packetId
    );

}