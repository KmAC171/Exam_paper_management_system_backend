package com.exam_paper.backend.repository;


import com.exam_paper.backend.dto.HodPacketResponseDTO;
import com.exam_paper.backend.dto.PacketDetailsResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;



public interface ExamPacketRepository
        extends JpaRepository<ExamPacket, Long> {



    // View all packets in HOD department

    @Query("""
            SELECT new com.exam_paper.backend.dto.HodPacketResponseDTO(

            ep.packetId,
            c.courseCode,
            c.courseName,
            ep.status,
            ep.deadline,
            u.name,
            ac.semester,
            ac.year

            )

            FROM ExamPacket ep

            JOIN ep.course c

            JOIN c.department d

            JOIN ep.currentHolder u

            JOIN ep.academicCycle ac

            WHERE d.departmentId = :departmentId

            """)
    List<HodPacketResponseDTO> findDepartmentPackets(
            @Param("departmentId") Long departmentId
    );



    // Search packets by course/status

    @Query("""
            SELECT ep

            FROM ExamPacket ep

            JOIN ep.course c

            JOIN c.department d

            WHERE d.departmentId = :departmentId

            AND
            (
            LOWER(c.courseName)
            LIKE LOWER(CONCAT('%',:keyword,'%'))

            OR

            LOWER(ep.status)
            LIKE LOWER(CONCAT('%',:keyword,'%'))
            )

            """)
    List<ExamPacket> searchPackets(
            @Param("departmentId") Long departmentId,
            @Param("keyword") String keyword
    );



    // Filter packets by status

    @Query("""
            SELECT ep

            FROM ExamPacket ep

            JOIN ep.course c

            JOIN c.department d

            WHERE d.departmentId=:departmentId

            AND ep.status=:status

            """)
    List<ExamPacket> filterByStatus(
            @Param("departmentId") Long departmentId,
            @Param("status") String status
    );



    // Full packet details

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
            holder.name,
            lecturer.name,
            moderator.name

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