package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.code.UserErrorCode;
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

    private static final long CODE_TTL_SECONDS = 300;
    private static final String AUTH_CODE_PREFIX = "auth:";
    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CoolSmsService coolSmsService;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtDTO generateTokensForUser(User user) {
        return jwtUtil.generateTokens(user.getId(), user.getRole());
    }

    /**
     * [회원가입용] 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다. (사용자 존재 여부 검증 없음)
     */
    @Override
    public String generateSignUpAuthCode(String phoneNumber) {
        // 1. 4자리 인증 코드 생성
        String code = String.format("%04d", secureRandom.nextInt(10000));

        // 2. Redis에 코드 저장 (키 통일)
        stringRedisTemplate.opsForValue()
                .set(AUTH_CODE_PREFIX + phoneNumber, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. CoolSMS 발송
        coolSmsService.sendVerificationSms(phoneNumber, code);
        return "인증 코드가 발송되었습니다.";
    }

    /**
     * [로그인용] 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다. (사용자 존재 여부 검증 있음)
     */
    @Override
    public String generateAuthCode(String phoneNumber) {
        if (!userRepository.existsByPhoneNumber((phoneNumber))) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND_USER);
        }

        // 1. 4자리 인증 코드 생성
        String code = String.format("%04d", secureRandom.nextInt(10000));

        // 2. Redis에 코드 저장 (키 통일)
        stringRedisTemplate.opsForValue()
                .set(AUTH_CODE_PREFIX + phoneNumber, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. CoolSMS 발송
        coolSmsService.sendVerificationSms(phoneNumber, code);

        return "인증 코드가 발송되었습니다.";
    }

    /**
     * 제출된 인증 코드를 Redis에 저장된 코드와 비교 검증합니다.
     */
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        String savedCode = stringRedisTemplate.opsForValue().get(AUTH_CODE_PREFIX + phoneNumber);

        if (savedCode != null && savedCode.equals(inputCode)) {
            stringRedisTemplate.delete(AUTH_CODE_PREFIX + phoneNumber);
            return true;
        }
        return false;
    }

    /**
     * 로그인 통합 처리: 인증 코드 검증 후 토큰 발급을 수행합니다.
     */
    @Override
    @Transactional
    public JwtDTO login(String phoneNumber, String authCode) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (!verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(UserErrorCode.INVALID_AUTH_CODE);
        }

        if (user.getStatus() != EUserStatus.ACTIVE) {
            user.activate();
        }

        return generateTokensForUser(user);
    }
}