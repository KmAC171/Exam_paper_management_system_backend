package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Unread notifications for user
    List<Notification> findByUserUserIdAndStatus(Long userId, String status);

    // All notifications sorted
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    // Count unread
    long countByUserUserIdAndStatus(Long userId, String status);
}