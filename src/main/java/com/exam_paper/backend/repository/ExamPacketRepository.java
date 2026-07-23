package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExamPacketRepository extends JpaRepository<ExamPacket, Long> {

    List<ExamPacket> findByLecturerUserId(Long lecturerId);

    boolean existsByPacketIdAndLecturerUserId(Long packetId, Long lecturerId);

    List<ExamPacket> findByLecturerUserIdAndSemesterCurrentTrue(Long lecturerId);

    long countByLecturerUserIdAndSemesterCurrentTrue(Long lecturerId);

    long countByLecturerUserIdAndStatusStatusName(Long lecturerId, String statusName);

    List<ExamPacket> findByLecturerUserIdAndSemesterSemesterId(Long lecturerId, Long semesterId);

    List<ExamPacket> findByLecturerUserIdAndCourseCourseCodeContainingIgnoreCase(Long lecturerId, String courseCode);

    List<ExamPacket> findByLecturerUserIdAndCourseCourseNameContainingIgnoreCase(Long lecturerId, String courseName);

    List<ExamPacket> findByLecturerUserIdAndStatusStatusNameIgnoreCase(Long lecturerId, String statusName);

    List<ExamPacket> findByLecturerUserIdAndDeadline(Long lecturerId, LocalDate deadline);

    List<ExamPacket> findByLecturerUserIdAndStatusStatusNameIn(Long lecturerId, List<String> statusNames);

    @Query("""
        SELECT p
        FROM ExamPacket p
        WHERE p.lecturer.userId = :lecturerId
        AND p.deadline < :today
        AND p.status.statusName <> :status
    """)
    List<ExamPacket> findOverduePackets(
            @Param("lecturerId") Long lecturerId,
            @Param("today") LocalDate today,
            @Param("status") String status
    );

    @Query("""
        SELECT COUNT(p)
        FROM ExamPacket p
        WHERE p.lecturer.userId = :lecturerId
        AND p.deadline < :today
        AND p.status.statusName <> :status
    """)
    long countOverduePackets(
            @Param("lecturerId") Long lecturerId,
            @Param("today") LocalDate today,
            @Param("status") String status
    );
}