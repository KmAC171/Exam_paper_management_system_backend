package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;

public interface BlackListedTokenRepository extends JpaRepository<BlackListedToken, Long> {
    boolean existsByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedToken b WHERE b.expiresAt < :now")
    void deleteExpiredTokens(Data now);

}
