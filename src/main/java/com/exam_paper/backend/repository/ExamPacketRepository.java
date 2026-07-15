package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.ExamPacket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;



public interface ExamPacketRepository
        extends JpaRepository<ExamPacket, Long> {



    // Filter packets by status
    List<ExamPacket> findByStatus(String status);




    // Search packets by:
    // course name
    // course code
    // packet status
    // lecturer name
    // moderator name

    @Query("""
            SELECT DISTINCT p
            FROM ExamPacket p
            LEFT JOIN PacketAssignment pa
                ON pa.packet = p
            LEFT JOIN pa.user u
            WHERE 
            LOWER(p.course.courseName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))

            OR LOWER(p.course.courseCode)
                LIKE LOWER(CONCAT('%', :keyword, '%'))

            OR LOWER(p.status)
                LIKE LOWER(CONCAT('%', :keyword, '%'))

            OR LOWER(u.fullName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<ExamPacket> searchPackets(
            @Param("keyword") String keyword
    );




    // Get packets assigned to a lecturer
    @Query("""
            SELECT p
            FROM ExamPacket p
            JOIN PacketAssignment pa
                ON pa.packet = p
            WHERE pa.user.userId = :lecturerId
            AND pa.assignedRole = 'Lecturer'
            """)
    List<ExamPacket> findPacketsAssignedToLecturer(
            @Param("lecturerId") Long lecturerId
    );



}