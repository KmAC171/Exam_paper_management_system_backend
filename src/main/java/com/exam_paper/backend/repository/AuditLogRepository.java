package com.exam_paper.backend.repository;


import com.example.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog,String>{



    List<AuditLog> findByUserUserId(String userId);



}