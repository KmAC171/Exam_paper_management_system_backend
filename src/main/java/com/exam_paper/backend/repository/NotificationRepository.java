package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, String> {


    List<Notification> findByUserUserIdOrderByCreatedAtDesc(String userId);


    List<Notification> findByUserUserIdAndStatus(
            String userId,
            String status
    );

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.status = 'Read'
        WHERE n.user.userId = :userId
    """)
    int markAllAsRead(@Param("userId") String userId);

}