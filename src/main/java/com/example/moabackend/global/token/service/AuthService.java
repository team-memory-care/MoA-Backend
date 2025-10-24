package com.example.moabackend.global.token.service;

public interface AuthService {
    // 인증 코드 생성
    String generateAuthCode(String phoneNumber);

    // 인증 코드 검증
    boolean verifyAuthCode(String phoneNumber, String inputCode);

    // 로그인 API
    String login(String phoneNumber, String authCode);
}
