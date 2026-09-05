package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacketRepository extends JpaRepository<ExamPacket, Long> {

    //AR & HOD
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status")
    List<ExamPacket> findAllWithDetails();

    //lecturer
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer l " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE l.userId = :userId")
    List<ExamPacket> findByLecturerId(@Param("userId") Long userId);

    //moderator
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator m " +
            "LEFT JOIN FETCH p.status " +
            "WHERE m.userId = :userId")
    List<ExamPacket> findByModeratorId(@Param("userId") Long userId);

    //department (HOD)
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE c.department.departmentId = :deptId")
    List<ExamPacket> findByDepartmentIdWithDetails(@Param("deptId") Long deptId);

    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE p.packetId = :id")
    Optional<ExamPacket> findByIdWithDetails(@Param("id") Long id);

    long countByCourse_CourseId(Long courseId);

    boolean existsByCourse_CourseId(Long courseId);

    boolean existsByCourse_CourseIdAndPacketIdNot(Long courseId, Long packetId);

    long countByCourse_Department_DepartmentId(Long departmentId);

    boolean existsByCourse_Department_DepartmentId(Long departmentId);

    List<ExamPacket> findByCourse_CourseId(Long courseId);

    List<ExamPacket> findByLecturer_UserId(Long userId);

    List<ExamPacket> findByModerator_UserId(Long userId);
}
