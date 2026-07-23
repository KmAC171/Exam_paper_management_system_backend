package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.ExamPacket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;



public interface ExamPacketRepository
        extends JpaRepository<ExamPacket, Long> {



    // =====================================
    // Filter by status
    // =====================================

    List<ExamPacket> findByStatus(String status);






    // =====================================
    // Advanced filtering
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
            ))


            AND

            (:moderatorId IS NULL
            OR (
                pa.assignedRole = 'Moderator'
                AND u.userId = :moderatorId
            ))

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
    // =====================================

    @Query("""
            SELECT DISTINCT p
            FROM ExamPacket p

            LEFT JOIN PacketAssignment pa
                ON pa.packet = p

            LEFT JOIN pa.user u

            WHERE

            LOWER(p.course.courseName)
            LIKE LOWER(CONCAT('%',:keyword,'%'))


            OR LOWER(p.course.courseCode)
            LIKE LOWER(CONCAT('%',:keyword,'%'))


            OR LOWER(p.status)
            LIKE LOWER(CONCAT('%',:keyword,'%'))


            OR LOWER(u.fullName)
            LIKE LOWER(CONCAT('%',:keyword,'%'))

            """)
    List<ExamPacket> searchPackets(
            @Param("keyword")
            String keyword
    );








    // =====================================
    // Lecturer packets
    // =====================================

    @Query("""
            SELECT p
            FROM ExamPacket p

            JOIN PacketAssignment pa
            ON pa.packet = p

            WHERE pa.user.userId = :lecturerId

            AND pa.assignedRole='Lecturer'

            """)
    List<ExamPacket> findPacketsAssignedToLecturer(

            @Param("lecturerId")
            Long lecturerId

    );








    // =====================================
    // Previous academic records
    // =====================================

    List<ExamPacket> findByAcademicCycleCycleId(
            Long cycleId
    );








    // =====================================
    // Overdue packets
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
    // =====================================

    @Query("""
            SELECT p
            FROM ExamPacket p

            WHERE p.deadline < CURRENT_DATE

            AND (
                p.status='Pending'

                OR

                p.status='In Progress'
            )

            """)
    List<ExamPacket> findDelayedPackets();








    // =====================================
    // Staff Performance Statistics
    // =====================================

    @Query("""
            SELECT

            pa.user.userId,

            pa.user.fullName,

            pa.assignedRole,


            SUM(
                CASE
                WHEN p.status='Completed'
                THEN 1
                ELSE 0
                END
            ),


            SUM(
                CASE
                WHEN p.status='Pending'
                THEN 1
                ELSE 0
                END
            ),


            SUM(
                CASE
                WHEN p.deadline < CURRENT_DATE
                AND p.status <> 'Completed'
                THEN 1
                ELSE 0
                END
            )


            FROM PacketAssignment pa


            JOIN pa.packet p


            GROUP BY

            pa.user.userId,

            pa.user.fullName,

            pa.assignedRole

            """)
    List<Object[]> getStaffPerformance();
// =====================================
// Report statistics
// =====================================


    @Query("""
        SELECT COUNT(p)
        FROM ExamPacket p
        """)
    Long countTotalPackets();




    @Query("""
        SELECT COUNT(p)
        FROM ExamPacket p
        WHERE p.status='Completed'
        """)
    Long countCompletedPackets();




    @Query("""
        SELECT COUNT(p)
        FROM ExamPacket p
        WHERE p.status='Pending'
        """)
    Long countPendingPackets();




    @Query("""
        SELECT COUNT(p)
        FROM ExamPacket p
        WHERE p.status='In Progress'
        """)
    Long countInProgressPackets();




    @Query("""
        SELECT COUNT(p)
        FROM ExamPacket p
        WHERE p.deadline < CURRENT_DATE
        AND p.status <> 'Completed'
        """)
    Long countOverduePackets();


}