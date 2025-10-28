package com.example.moabackend.global.token.service;

import com.example.moabackend.domain.user.dto.UserSecurityForm;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TwilioVerificationService twilioVerificationService;

    /**
     * 인증 코드를 생성하고 Redis에 저장합니다.
     */
    @Override
    public String generateAuthCode(String phoneNumber) {
        twilioVerificationService.requestVerificationCode(phoneNumber);
        return "인증 코드가 발송되었습니다.";
    }

    /**
     * 제출된 인증 코드와 저장된 코드를 비교하고, 성공 시 Redis에서 삭제합니다.
     */
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        return twilioVerificationService.checkVerificationCode(phoneNumber, inputCode);
    }

    /**
     * 로그인 통합 처리: authCode 유무에 따라 발송 또는 검증/토큰 발급을 수행합니다.
     */
    @Override
    @Transactional // 사용자 상태 변경(activate)을 포함하므로 쓰기 트랜잭션 필요
    public JwtDTO login(String phoneNumber, String authCode) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (!verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
        }

        // 인증 성공: User 상태 ACTIVE로 변경 (더티 체킹)
        user.activate();

        // 1. JWT에 담을 사용자 정보 추출
        UserSecurityForm securityForm = UserSecurityForm.from(user);
        // 2. Access Token과 Refresh Token을 포함하는 DTO 생성 및 반환
        return jwtUtil.generateTokens(user.getId(), user.getRole());
    }
}
