package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PrintingSchedule;
import com.exam_paper.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrintingScheduleRepository extends JpaRepository<PrintingSchedule, Long> {

    List<PrintingSchedule> findByPacketPacketId(Long packetId);

    List<PrintingSchedule> findByPacketPacketIdAndStatusNotIn(Long packetId, List<String> excludedStatuses);

    Optional<PrintingSchedule> findFirstByPacketPacketIdAndStatusNotInOrderByCreatedAtDesc(Long packetId, List<String> excludedStatuses);

    List<PrintingSchedule> findByLecturerOrderByScheduleDateDescStartTimeDesc(User lecturer);

    List<PrintingSchedule> findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(LocalDate fromDate, LocalDate toDate);

    List<PrintingSchedule> findByScheduleDateAndLocationAndStatusNotIn(LocalDate scheduleDate, String location, List<String> excludedStatuses);

    List<PrintingSchedule> findByScheduleDateAndStatusNotIn(LocalDate scheduleDate, List<String> excludedStatuses);

    @Query("SELECT s FROM PrintingSchedule s WHERE s.scheduleDate = :date AND s.location = :location " +
           "AND s.status NOT IN ('CANCELLED', 'MISSED') " +
           "AND (:excludeId IS NULL OR s.scheduleId != :excludeId) " +
           "AND (s.startTime < :endTime AND s.endTime > :startTime)")
    List<PrintingSchedule> findOverlappingActiveSchedules(
            @Param("date") LocalDate date,
            @Param("location") String location,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    long countByScheduleDate(LocalDate date);

    long countByScheduleDateAndStatus(LocalDate date, String status);

    long countByStatus(String status);
}
