package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.ExamPacket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;



public interface ExamPacketRepository
        extends JpaRepository<ExamPacket, Long> {



    // =====================================
    // Filter packets by status
    // =====================================

    List<ExamPacket> findByStatus(String status);





    // =====================================
    // Advanced packet filtering
    // status + academic cycle + lecturer + moderator
    // =====================================

    @Query("""
            SELECT DISTINCT p
            FROM ExamPacket p

            LEFT JOIN PacketAssignment pa
                ON pa.packet = p

            LEFT JOIN pa.user u


            WHERE

            (:status IS NULL
                OR p.status = :status)


            AND

            (:cycleId IS NULL
                OR p.academicCycle.cycleId = :cycleId)


            AND

            (:lecturerId IS NULL
                OR (
                    pa.assignedRole = 'Lecturer'
                    AND u.userId = :lecturerId
                )
            )


            AND

            (:moderatorId IS NULL
                OR (
                    pa.assignedRole = 'Moderator'
                    AND u.userId = :moderatorId
                )
            )

            """)
    List<ExamPacket> filterPackets(

            @Param("status")
            String status,


            @Param("cycleId")
            Long cycleId,


            @Param("lecturerId")
            Long lecturerId,


            @Param("moderatorId")
            Long moderatorId

    );






    // =====================================
    // Search packets
    // course name
    // course code
    // status
    // lecturer/moderator name
    // =====================================

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

            @Param("keyword")
            String keyword

    );







    // =====================================
    // Get packets assigned to lecturer
    // =====================================

    @Query("""
            SELECT p
            FROM ExamPacket p

            JOIN PacketAssignment pa
                ON pa.packet = p

            WHERE pa.user.userId = :lecturerId

            AND pa.assignedRole = 'Lecturer'

            """)
    List<ExamPacket> findPacketsAssignedToLecturer(

            @Param("lecturerId")
            Long lecturerId

    );








    // =====================================
    // Previous academic cycle packets
    // =====================================

    List<ExamPacket> findByAcademicCycleCycleId(

            Long cycleId

    );








    // =====================================
    // Overdue packets
    //
    // deadline passed
    // status is not completed
    // =====================================

    @Query("""
            SELECT p
            FROM ExamPacket p

            WHERE p.deadline < CURRENT_DATE

            AND p.status <> 'Completed'

            """)
    List<ExamPacket> findOverduePackets();








    // =====================================
    // Delayed packets
    //
    // deadline passed
    // still pending/in progress
    // =====================================

    @Query("""
            SELECT p
            FROM ExamPacket p

            WHERE p.deadline < CURRENT_DATE

            AND (
                p.status = 'Pending'

                OR

                p.status = 'In Progress'
            )

            """)
    List<ExamPacket> findDelayedPackets();



}