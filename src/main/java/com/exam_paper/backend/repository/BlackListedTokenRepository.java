package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface BlackListedTokenRepository extends JpaRepository<BlackListedToken, Long> {

    boolean existsByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlackListedToken b WHERE b.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Date now);
}