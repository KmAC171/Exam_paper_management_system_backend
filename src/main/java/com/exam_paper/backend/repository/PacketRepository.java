package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ExamPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacketRepository extends JpaRepository<ExamPacket, Long> {

    // AR & HOD - all
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status")
    List<ExamPacket> findAllWithDetails();

    // AR & HOD - filtered by Cycle
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle ac " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE ac.cycleId = :cycleId")
    List<ExamPacket> findAllWithDetailsByCycleId(@Param("cycleId") String cycleId);

    // lecturer
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer l " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE l.userId = :userId")
    List<ExamPacket> findByLecturerId(@Param("userId") Long userId);

    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle ac " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer l " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE l.userId = :userId AND ac.cycleId = :cycleId")
    List<ExamPacket> findByLecturerIdAndCycleId(@Param("userId") Long userId, @Param("cycleId") String cycleId);

    // moderator
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator m " +
            "LEFT JOIN FETCH p.status " +
            "WHERE m.userId = :userId")
    List<ExamPacket> findByModeratorId(@Param("userId") Long userId);

    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle ac " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator m " +
            "LEFT JOIN FETCH p.status " +
            "WHERE m.userId = :userId AND ac.cycleId = :cycleId")
    List<ExamPacket> findByModeratorIdAndCycleId(@Param("userId") Long userId, @Param("cycleId") String cycleId);

    // lecturer or moderator (unified academic staff)
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer l " +
            "LEFT JOIN FETCH p.moderator m " +
            "LEFT JOIN FETCH p.status " +
            "WHERE (l.userId = :userId OR m.userId = :userId)")
    List<ExamPacket> findByLecturerOrModeratorId(@Param("userId") Long userId);

    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle ac " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer l " +
            "LEFT JOIN FETCH p.moderator m " +
            "LEFT JOIN FETCH p.status " +
            "WHERE (l.userId = :userId OR m.userId = :userId) AND ac.cycleId = :cycleId")
    List<ExamPacket> findByLecturerOrModeratorIdAndCycleId(@Param("userId") Long userId, @Param("cycleId") String cycleId);

    // department (HOD)
    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE c.department.departmentId = :deptId")
    List<ExamPacket> findByDepartmentIdWithDetails(@Param("deptId") Long deptId);

    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle ac " +
            "LEFT JOIN FETCH p.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH p.lecturer " +
            "LEFT JOIN FETCH p.moderator " +
            "LEFT JOIN FETCH p.status " +
            "WHERE c.department.departmentId = :deptId AND ac.cycleId = :cycleId")
    List<ExamPacket> findByDepartmentIdAndCycleId(@Param("deptId") Long deptId, @Param("cycleId") String cycleId);

    @Query("SELECT p FROM ExamPacket p " +
            "LEFT JOIN FETCH p.academicCycle " +
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

    boolean existsByCourse_CourseIdAndAcademicCycle_CycleId(Long courseId, String cycleId);

    Optional<ExamPacket> findByCourse_CourseIdAndAcademicCycle_CycleId(Long courseId, String cycleId);

    long countByCourse_Department_DepartmentId(Long departmentId);

    boolean existsByCourse_Department_DepartmentId(Long departmentId);

    List<ExamPacket> findByCourse_CourseId(Long courseId);

    List<ExamPacket> findByAcademicCycle_CycleId(String cycleId);

    long countByAcademicCycle_CycleId(String cycleId);

    long countByAcademicCycle_CycleIdAndStatus_StatusName(String cycleId, String statusName);

    List<ExamPacket> findByLecturer_UserId(Long userId);

    List<ExamPacket> findByModerator_UserId(Long userId);
}

