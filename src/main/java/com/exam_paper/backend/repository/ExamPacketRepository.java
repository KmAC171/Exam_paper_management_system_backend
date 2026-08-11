package com.exam_paper.backend.repository;

import com.example.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamPacketRepository
        extends JpaRepository<ExamPacket, String> {

    // Find packets by status
    List<ExamPacket> findByStatus(String status);

    // Find packets belonging to an academic cycle
    List<ExamPacket> findByAcademicCycleCycleId(String cycleId);

    // Find packets where a user is the current holder
    List<ExamPacket> findByCurrentHolderUserId(String userId);

    // Find overdue packets
    List<ExamPacket> findByDeadlineBefore(LocalDate date);

    /*
        Lecturer Feature:
        View packets assigned to a lecturer
        for a specific academic cycle
    */
    @Query("""
            SELECT ep
            FROM ExamPacket ep
            JOIN PacketAssignment pa
            ON pa.packet = ep
            WHERE pa.user.userId = :userId
            AND ep.academicCycle.cycleId = :cycleId
            """)
    List<ExamPacket> findAssignedPacketsByLecturerAndCycle(
            @Param("userId") String userId,
            @Param("cycleId") String cycleId
    );

    // Find packet by packet ID
    Optional<ExamPacket> findByPacketId(String packetId);

    // Find packets by academic cycle status
    List<ExamPacket> findByAcademicCycleStatus(String status);

    /*
        Search packets by course code or course name
    */
    @Query("""
            SELECT p
            FROM ExamPacket p
            JOIN p.course c
            WHERE LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<ExamPacket> searchPackets(
            @Param("keyword") String keyword
    );

    /*
        Filter packets by status, deadline, and academic cycle
    */
    @Query("""
        SELECT p
        FROM ExamPacket p
        JOIN p.academicCycle a
        WHERE
        (:status IS NULL OR p.status = :status)
        AND (:deadline IS NULL OR p.deadline = :deadline)
        AND (
             :cycle IS NULL
             OR a.cycleId = :cycle
             OR CONCAT(a.year,'-',a.semester)=:cycle
        )
    """)
    List<ExamPacket> filterPackets(
            @Param("status") String status,
            @Param("deadline") LocalDate deadline,
            @Param("cycle") String cycle
    );
}
