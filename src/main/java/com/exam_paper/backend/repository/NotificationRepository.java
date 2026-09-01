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
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId OR n.packet.lecturer.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrPacketLecturer(@Param("userId") Long userId);

    // Moderator — notifications for their assigned packets
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId OR n.packet.moderator.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrPacketModerator(@Param("userId") Long userId);

    long countByIsReadFalse();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false AND (n.user.userId = :userId OR n.packet.moderator.userId = :userId)")
    long countUnreadByModeratorId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false AND (n.user.userId = :userId OR n.packet.lecturer.userId = :userId)")
    long countUnreadByLecturerId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true")
    void markAllAsRead();

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId OR n.packet.moderator.userId = :userId")
    void markAllAsReadForModerator(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId OR n.packet.lecturer.userId = :userId")
    void markAllAsReadForLecturer(@Param("userId") Long userId);
}