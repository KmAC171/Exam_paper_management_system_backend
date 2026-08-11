package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
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

}