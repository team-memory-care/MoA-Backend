package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.dto.req.LogoutRequestDto;
import com.example.moabackend.global.token.dto.req.ReissueTokenRequestDto;
import com.example.moabackend.global.token.dto.res.ReissueTokenResponseDto;

public interface AuthService {

    /**
     * [회원가입용] 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다.
     */
    String generateSignUpAuthCode(String phoneNumber);

    /**
     * [로그인용] 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다. (사용자 존재 여부 검증 있음)
     */
    String generateAuthCode(String phoneNumber);

    /**
     * 제출된 인증 코드를 Redis에 저장된 코드와 비교 검증합니다.
     */
    boolean verifyAuthCode(String phoneNumber, String inputCode);

    /**
     * 특정 사용자 엔티티를 기반으로 JWT 액세스/리프레시 토큰을 생성합니다.
     */
    JwtDTO generateTokensForUser(User user);

    /**
     * 로그인 통합 처리: 인증 코드 검증 후 토큰 발급을 수행합니다.
     */
    JwtDTO login(String phoneNumber, String authCode);

    ReissueTokenResponseDto reissueToken(ReissueTokenRequestDto requestDto);

    void logout(String accessToken, LogoutRequestDto requestDto);

    void withdraw(Long userId);
}