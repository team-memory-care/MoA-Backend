// src/main/java/com/example.moabackend.global.token.service/AuthServiceImpl.java
package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CoolSmsService coolSmsService;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final long CODE_TTL_SECONDS = 300;
    private static final String AUTH_CODE_PREFIX = "auth:";

    public JwtDTO generateTokensForUser(User user) {
        return jwtUtil.generateTokens(user.getId(), user.getRole());
    }

    /**
     * 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다.
     */
    @Override
    public String generateAuthCode(String phoneNumber) {
        // 1. 4자리 인증 코드 생성 (0000~9999)
        String code = String.format("%04d", secureRandom.nextInt(10000));

        // 2. Redis에 코드 저장 (TTL 5분 설정)
        stringRedisTemplate.opsForValue()
                .set("auth:" + phoneNumber, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. CoolSMS 발송
        coolSmsService.sendVerificationSms(phoneNumber, code);

        return "인증 코드가 발송되었습니다.";
    }

    /**
     * 제출된 인증 코드를 Redis에 저장된 코드와 비교 검증합니다.
     */
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        // ✅ 버그 수정: Redis 키 조회 시 공백 제거 (generateAuthCode와 키 통일)
        String savedCode = stringRedisTemplate.opsForValue().get("auth:" + phoneNumber);

        // 코드 일치 확인 및 Redis 키 삭제 (일회성 사용)
        if (savedCode != null && savedCode.equals(inputCode)) {
            stringRedisTemplate.delete("auth:" + phoneNumber);
            return true;
        }
        return false;
    }

    /**
     * 로그인 통합 처리: 인증 코드 검증 후 토큰 발급을 수행합니다.
     */
    @Override
    @Transactional // 사용자 상태 변경(activate)을 포함하므로 쓰기 트랜잭션 필요
    public JwtDTO login(String phoneNumber, String authCode) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (!verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
        }

        if (user.getStatus() != EUserStatus.ACTIVE) {
            user.activate();
        }

        return generateTokensForUser(user);
    }
}