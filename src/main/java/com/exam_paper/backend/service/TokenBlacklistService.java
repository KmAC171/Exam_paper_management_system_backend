package com.exam_paper.backend.service;

import com.exam_paper.backend.entity.BlackListedToken;
import com.exam_paper.backend.repository.BlackListedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final BlackListedTokenRepository blackListedTokenRepository;

    public void blacklistToken (String token, Date expiresAt){
        BlackListedToken blacklisted = BlackListedToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
        blackListedTokenRepository.save(blacklisted);
    }

    public boolean isBlacklisted(String token){
        return blackListedTokenRepository.existsByToken(token);
    }

    @Scheduled(fixedRate = 360000)
    public void cleanExpiredTokens() {
        blackListedTokenRepository.deleteExpiredTokens(new Date());d
        System.out.println(">>> Cleaned up expired blacklisted tokens");
    }

}
