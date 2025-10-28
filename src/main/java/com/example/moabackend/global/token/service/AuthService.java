package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.global.security.dto.JwtDTO;

public interface AuthService {
    // 인증 코드 생성
    String generateAuthCode(String phoneNumber);

    // 인증 코드 검증
    boolean verifyAuthCode(String phoneNumber, String inputCode);

    // 로그인 API
    JwtDTO login(String phoneNumber, String authCode);

    JwtDTO generateTokensForUser(User user);
}