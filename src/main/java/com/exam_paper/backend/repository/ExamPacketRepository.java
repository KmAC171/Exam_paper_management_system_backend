package com.exam_paper.backend.repository;

import com.exam_paper.backend.dto.DepartmentStatsDto;
import com.exam_paper.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExamPacketRepository extends JpaRepository<ExamPacket, Long> {

    List<ExamPacket> findByLecturerUserId(Long lecturerId);
    long countByStatus_StatusName(String statusName);

    @Query("SELECT COUNT(p) FROM ExamPacket p " + "WHERE p.status.statusName = 'PENDING'AND p.deadline < :today ")
    long countDelayed(@Param("today") LocalDate today);

    @Query(value = """
    SELECT 
        d.department_name AS departmentName,
        COUNT(p.packet_id) AS submitted,
        SUM(CASE WHEN ps.status_name = 'APPROVED' THEN 1 ELSE 0 END) AS approved,
        SUM(CASE WHEN ps.status_name = 'PENDING' AND p.deadline < :today THEN 1 ELSE 0 END) AS `delayed`
    FROM exam_packets p
    JOIN courses c ON p.course_id = c.course_id
    JOIN departments d ON c.department_id = d.department_id
    JOIN packet_status ps ON p.status_id = ps.status_id
    GROUP BY d.department_id, d.department_name
    """, nativeQuery = true)
    List<DepartmentStatsProjection> getDepartmentStats(@Param("today") LocalDate today);

    @Query(value = """
    SELECT 
        MONTH(p.deadline) AS month,
        COUNT(*) AS count
    FROM exam_packets p
    WHERE YEAR(p.deadline) = YEAR(CURDATE())
    GROUP BY MONTH(p.deadline)
    ORDER BY MONTH(p.deadline)
    """, nativeQuery = true)
    List<SubmissionTrendProjection> getSubmissionTrend();

}