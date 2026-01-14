package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.security.service.RefreshTokenService;
import com.example.moabackend.global.security.utils.JwtUtil;
import com.example.moabackend.global.token.dto.req.ReissueTokenRequestDto;
import com.example.moabackend.global.token.dto.res.ReissueTokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final long CODE_TTL_SECONDS = 300;
    private static final String AUTH_CODE_PREFIX = "auth:";
    private static final String TEST_ACCOUNT_NUMBER = "821035477120";
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]");
    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CoolSmsService coolSmsService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenDenyService accessTokenDenyService;


    public JwtDTO generateTokensForUser(User user) {
        JwtDTO jwtDto = jwtUtil.generateTokens(user.getId(), user.getRole());
        refreshTokenService.saveRefreshToken(user.getId(), jwtDto.refreshToken());
        return jwtDto;
    }

    private String resolveTestNumber(String phoneNumber) {
        String clean = NON_DIGIT_PATTERN.matcher(phoneNumber).replaceAll("");

        if (clean.equals("01035477120") ||
                clean.equals("821035477120") ||
                clean.equals("8201035477120")) {
            return TEST_ACCOUNT_NUMBER;
        }
        return phoneNumber;
    }

    /**
     * [회원가입용] 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다. (사용자 존재 여부 검증 없음)
     */
    @Override
    public String generateSignUpAuthCode(String phoneNumber) {
        String resolvedNumber = resolveTestNumber(phoneNumber);
        String code;
        // 1. 4자리 인증 코드 생성
        if (TEST_ACCOUNT_NUMBER.equals(resolvedNumber)) {
            code = "0911";
        } else {
            code = String.format("%04d", secureRandom.nextInt(10000));
        }

        // 2. Redis에 코드 저장 (키 통일)
        stringRedisTemplate.opsForValue()
                .set(AUTH_CODE_PREFIX + resolvedNumber, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. CoolSMS 발송
        if (!TEST_ACCOUNT_NUMBER.equals(resolvedNumber)) {
            coolSmsService.sendVerificationSms(phoneNumber, code);
        }
        return "인증 코드가 발송되었습니다.";
    }

    /**
     * [로그인용] 인증 코드를 생성, Redis에 저장하고 CoolSMS로 발송합니다. (사용자 존재 여부 검증 있음)
     */
    @Override
    public String generateAuthCode(String phoneNumber) {
        String resolvedNumber = resolveTestNumber(phoneNumber);
        if (!TEST_ACCOUNT_NUMBER.equals(resolvedNumber)) {
            if (!userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new CustomException(GlobalErrorCode.NOT_FOUND_USER);
            }
        }
        String code;

        // 1. 4자리 인증 코드 생성
        if (TEST_ACCOUNT_NUMBER.equals(resolvedNumber)) {
            code = "0911";
        } else {
            code = String.format("%04d", secureRandom.nextInt(10000));
        }

        // 2. Redis에 코드 저장 (키 통일)
        stringRedisTemplate.opsForValue()
                .set(AUTH_CODE_PREFIX + resolvedNumber, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. CoolSMS 발송
        if (!TEST_ACCOUNT_NUMBER.equals(resolvedNumber)) {
            coolSmsService.sendVerificationSms(phoneNumber, code);
        }
        return "인증 코드가 발송되었습니다.";
    }

    /**
     * 제출된 인증 코드를 Redis에 저장된 코드와 비교 검증합니다.
     */
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        String resolvedNumber = resolveTestNumber(phoneNumber);
        String failCountKey = AUTH_CODE_PREFIX + resolvedNumber + ": fail";
        String savedCode = stringRedisTemplate.opsForValue().get(AUTH_CODE_PREFIX + resolvedNumber);

        if (savedCode != null && savedCode.equals(inputCode)) {
            stringRedisTemplate.delete(AUTH_CODE_PREFIX + resolvedNumber);
            stringRedisTemplate.delete(failCountKey);
            return true;
        }

        // 틀렸을 경우, 실패 카운트 증가
        if (savedCode != null) {
            Long count = stringRedisTemplate.opsForValue().increment(failCountKey);
            if (count != null && count >= 5) {
                stringRedisTemplate.delete(AUTH_CODE_PREFIX + resolvedNumber);
                stringRedisTemplate.delete(failCountKey);
                throw new CustomException(UserErrorCode.AUTH_CODE_EXPIRED);
            }
        }
        return false;
    }

    /**
     * 로그인 통합 처리: 인증 코드 검증 후 토큰 발급을 수행합니다.
     */
    @Override
    @Transactional
    public JwtDTO login(String phoneNumber, String authCode) {
        String resolvedNumber = resolveTestNumber(phoneNumber);
        User user = userRepository.findByPhoneNumber(resolvedNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        // 테스트 계정 프리패스: 0911인 경우에만 통과
        if (TEST_ACCOUNT_NUMBER.equals(resolvedNumber)) {
            if (!"0911".equals(authCode)) {
                throw new CustomException(UserErrorCode.INVALID_AUTH_CODE);
            }
        } else if (!verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(UserErrorCode.INVALID_AUTH_CODE);
        }
        if (user.getStatus() == EUserStatus.WITHDRAWN) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND_USER);
        }
        if (user.getStatus() != EUserStatus.ACTIVE) {
            user.activate();
        }

        return generateTokensForUser(user);
    }

    @Override
    @Transactional
    public ReissueTokenResponseDto reissueToken(ReissueTokenRequestDto requestDto) {
        String refreshToken = requestDto.refreshToken();

        Long userId = jwtUtil.validateRefreshToken(refreshToken);

        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new CustomException(GlobalErrorCode.INVALID_TOKEN_ERROR);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));
        ERole role = user.getRole();

        JwtDTO jwtDto = jwtUtil.generateTokens(userId, role);

        refreshTokenService.saveRefreshToken(userId, jwtDto.refreshToken());

        return ReissueTokenResponseDto.from(jwtDto.accessToken(), jwtDto.refreshToken(), role);
    }

    @Override
    @Transactional
    public void logout(String accessToken, Long userId) {
        String jti = jwtUtil.getJti(accessToken);
        long expire = jwtUtil.getAccessTokenRemainingMillis(accessToken);
        if (expire > 0) {
            accessTokenDenyService.deny(jti, Duration.ofSeconds(expire));
        }

        refreshTokenService.deleteRefreshToken(userId);

        userRepository.findById(userId).ifPresent(user -> user.updateFcmToken(null));
    }

    @Override
    @Transactional
    public void withdraw(Long userId) {
        refreshTokenService.deleteRefreshToken(userId);

        String accessToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

        String jti = jwtUtil.getJti(accessToken);
        long expire = jwtUtil.getAccessTokenRemainingMillis(accessToken);
        if (expire > 0) {
            accessTokenDenyService.deny(jti, Duration.ofSeconds(expire));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        user.withdraw();
    }
}