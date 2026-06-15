package com.exam_paper.backend.service;

import com.exam_paper.backend.repository.BlackListedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final BlackListedTokenRepository blackListedTokenRepository;

    public void blacklistToken (String token, Date expiresAt){
        
    }

}
