// src/main/java/com/example/moabackend.domain.user.service/UserServiceImpl.java
package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.service.AuthService;
import com.example.moabackend.global.token.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final RedisService redisService;
    private static final long SIGNUP_TTL_MINUTES = 5;

    /**
     * 중복되지 않는 4자리 부모 회원 코드를 생성합니다. (DB 검사)
     */
    private String generateUniqueParentCode() {
        String code;
        do {
            code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        } while (userRepository.existsByParentCode(code));
        return code;
    }

    /**
     * [회원가입 1단계] 사용자 정보를 Redis에 임시 저장하고 인증 코드를 발송합니다.
     */
    @Override
    @Transactional
    public String preSignUpAndSendCode(UserSignUpRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
        }

        String redisKey = "signup:temp:" + request.phoneNumber();
        redisService.setData(redisKey, request, SIGNUP_TTL_MINUTES);

        return authService.generateAuthCode(request.phoneNumber());
    }

    @Override
    @Transactional
    public JwtDTO confirmSignUpAndLogin(String phoneNumber, String authCode) {
        // 1. 인증 코드 검증 (Redis 비교 및 삭제)
        if (!authService.verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
        }

        // 2. Redis에서 임시 DTO 조회
        String redisKey = "signup:temp:" + phoneNumber;
        UserSignUpRequest request = redisService.getData(redisKey, UserSignUpRequest.class);

        if (request == null) {
            throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
        }
        redisService.deleteData(redisKey);

        // 3. DB 저장
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        User user = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .role(request.role())
                .gender(request.gender())
                .status(EUserStatus.ACTIVE)
                .birthDate(parsed)
                .parentCode(null)
                .connectedParentCode(request.parentCode())
                .build();

        User savedUser = userRepository.save(user);
        return authService.generateTokensForUser(savedUser);
    }

    /**
     * 사용자 역할 선택 API: 미선택(PENDING) 상태에서 역할 확정 및 부모-자녀 연결을 수행합니다.
     */
    @Override
    @Transactional
    public UserResponseDto selectRoleAndLinkParent(Long userId, ERole role, String parentCode) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        // 역할 선택은 최초 1번만 허용
        if (user.getRole() != ERole.PENDING) {
            throw new CustomException(GlobalErrorCode.ALREADY_ROLE_SELECTED);
        }

        String codeToIssue = null;
        String codeToConnect = null;

        if (role == ERole.PARENT) {
            codeToIssue = generateUniqueParentCode();
        } else if (role == ERole.CHILD) {
            if (parentCode == null || !userRepository.existsByParentCode(parentCode)) {
                throw new CustomException(GlobalErrorCode.INVALID_PARENT_CODE);
            }
            codeToConnect = parentCode;
        } else {
            throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        user.completeRoleSelection(role, codeToIssue, codeToConnect);
        return UserResponseDto.from(user);
    }

    /**
     * 회원 코드 생성 API: 인증된 부모 사용자의 회원 코드를 조회하거나 발급합니다.
     */
    @Override
    @Transactional
    public String issueOrGetParentCode(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (user.getRole() != ERole.PARENT) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        if (user.getParentCode() != null) {
            return user.getParentCode();
        }

        String newCode = generateUniqueParentCode();
        user.setParentCode(newCode);
        return newCode;
    }
}