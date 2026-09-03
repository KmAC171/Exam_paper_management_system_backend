package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // AR/HOD — all notifications
    List<Notification> findAllByOrderByCreatedAtDesc();

    // Lecturer — notifications for their packets
    @Query("SELECT n FROM Notification n LEFT JOIN n.packet p LEFT JOIN p.lecturer l LEFT JOIN n.user u WHERE (u.userId = :userId OR l.userId = :userId) ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrPacketLecturer(@Param("userId") Long userId);

    // Moderator — notifications for their assigned packets
    @Query("SELECT n FROM Notification n LEFT JOIN n.packet p LEFT JOIN p.moderator m LEFT JOIN n.user u WHERE (u.userId = :userId OR m.userId = :userId) ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrPacketModerator(@Param("userId") Long userId);

    long countByIsReadFalse();

    @Query("SELECT COUNT(n) FROM Notification n LEFT JOIN n.packet p LEFT JOIN p.moderator m LEFT JOIN n.user u WHERE n.isRead = false AND (u.userId = :userId OR m.userId = :userId)")
    long countUnreadByModeratorId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n LEFT JOIN n.packet p LEFT JOIN p.lecturer l LEFT JOIN n.user u WHERE n.isRead = false AND (u.userId = :userId OR l.userId = :userId)")
    long countUnreadByLecturerId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true")
    void markAllAsRead();

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN (SELECT n2.id FROM Notification n2 LEFT JOIN n2.packet p LEFT JOIN p.moderator m LEFT JOIN n2.user u WHERE u.userId = :userId OR m.userId = :userId)")
    void markAllAsReadForModerator(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN (SELECT n2.id FROM Notification n2 LEFT JOIN n2.packet p LEFT JOIN p.lecturer l LEFT JOIN n2.user u WHERE u.userId = :userId OR l.userId = :userId)")
    void markAllAsReadForLecturer(@Param("userId") Long userId);
}