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
            "JOIN FETCH p.course c " +
            "JOIN FETCH c.department " +
            "JOIN FETCH p.lecturer " +
            "JOIN FETCH p.moderator " +
            "JOIN FETCH p.status")
    List<ExamPacket> findAllWithDetails();

    //lecturer
    @Query("SELECT p FROM ExamPacket p " +
            "JOIN FETCH p.course c " +
            "JOIN FETCH c.department " +
            "JOIN FETCH p.lecturer l " +
            "JOIN FETCH p.moderator " +
            "JOIN FETCH p.status " +
            "WHERE l.userId = :userId")
    List<ExamPacket> findByLecturerId(@Param("userId") Long userId);

    //moderator
    @Query("SELECT p FROM ExamPacket p " +
            "JOIN FETCH p.course c " +
            "JOIN FETCH c.department " +
            "JOIN FETCH p.lecturer " +
            "JOIN FETCH p.moderator m " +
            "JOIN FETCH p.status " +
            "WHERE m.userId = :userId")
    List<ExamPacket> findByModeratorId(@Param("userId") Long userId);

    @Query("SELECT p FROM ExamPacket p " +
            "JOIN FETCH p.course c " +
            "JOIN FETCH c.department " +
            "JOIN FETCH p.lecturer " +
            "JOIN FETCH p.moderator " +
            "JOIN FETCH p.status " +
            "WHERE p.packetId = :id")
    Optional<ExamPacket> findByIdWithDetails(@Param("id") Long id);
}
