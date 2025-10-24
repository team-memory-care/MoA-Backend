package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;

    /**
     * 인증 코드를 생성하고 Redis에 저장합니다.
     */
    @Override
    public String generateAuthCode(String phoneNumber) {
        // [NOTE] String.format("%04d", ...)가 4자리 코드를 생성하므로, nextInt 범위는 10000 이하가 적절합니다.
        String code = String.format("%04d", new Random().nextInt(10000));
        stringRedisTemplate.opsForValue()
                .set("auth:" + phoneNumber, code, 5, TimeUnit.MINUTES);
        return code;
    }

    /**
     * 제출된 인증 코드와 저장된 코드를 비교하고, 성공 시 Redis에서 삭제합니다.
     */
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        String savedCode = stringRedisTemplate.opsForValue().get("auth:" + phoneNumber);

        if (savedCode != null && savedCode.equals(inputCode)) {
            stringRedisTemplate.delete("auth:" + phoneNumber);
            return true;
        }
        return false;
    }

    /**
     * 로그인 통합 처리: authCode 유무에 따라 발송 또는 검증/토큰 발급을 수행합니다.
     */
    @Override
    @Transactional // 사용자 상태 변경(activate)을 포함하므로 쓰기 트랜잭션 필요
    public String login(String phoneNumber, String authCode) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (authCode == null || authCode.isBlank()) {
            // 인증 코드 발송 요청
            return generateAuthCode(phoneNumber);
        } else {
            // 인증 코드 검증 및 로그인 처리
            if (!verifyAuthCode(phoneNumber, authCode)) {
                throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
            }

            // 인증 성공: User 상태 ACTIVE로 변경 (더티 체킹)
            user.activate();

            // TODO: 실제 JWT 토큰 발급 로직으로 대체 필요
            // return jwtTokenProvider.generateToken(UserSecurityForm.from(user));
            return "JWT_TOKEN_FOR_" + user.getId();
        }
    }
}