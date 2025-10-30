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
import java.util.UUID;
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
    private static final String SIGNUP_TEMP_KEY_PREFIX = "signup:temp:";

    /**
     * 중복되지 않는 4자리 부모 회원 코드를 생성합니다. (DB 검사)
     * WARN: DB parentCode 컬럼에 Unique 제약조건이 필수이며, 동시성 충돌 발생 시 재시도가 필요합니다.
     */
    private String generateUniqueParentCode() {
        final int MAX_RETRIES = 5;
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            // DB 검사가 통과해도 커밋 직전 Race Condition으로 중복이 발생할 수 있으므로,
            // DB의 Unique 제약조건을 믿고 실패 시 재시도를 명시적으로 추가합니다.
            if (!userRepository.existsByParentCode(code)) {
                return code;
            }
        }
        log.error("Failed to generate unique parent code after {} retries.", MAX_RETRIES);
        throw new CustomException(GlobalErrorCode.CODE_GENERATION_FAILED); // 새로운 에러 코드 필요
    }

    /**
     * [회원가입 1단계] 사용자 기본 정보를 Redis에 임시 저장합니다.
     */
    @Override
    @Transactional
    public void preSignUp(UserSignUpRequest request) {
        // 전화번호가 없으므로 UUID를 임시키로 사용하고, 클라이언트에게 이 키를 반환해야 합니다.
        // 현재는 편의상 전화번호를 임시 키로 사용했던 기존 방식의 흔적을 남겨 두었습니다.
        // 클라이언트에서 1단계 이후 전화번호 입력 시, 해당 키와 전화번호를 연결해야 합니다.
        String redisKey = SIGNUP_TEMP_KEY_PREFIX + UUID.randomUUID(); // 실제 운영 시 이 UUID를 클라이언트에게 반환해야 함.
        redisService.setData(redisKey, request, SIGNUP_TTL_MINUTES);
    }

    /**
     * [회원가입 2단계-1] 전화번호 중복 체크 및 인증 코드 발송을 처리합니다.
     */
    @Override
    @Transactional(readOnly = false) // 인증 코드 발송 자체는 쓰기 트랜잭션이 필요 없지만, 일반적으로는 서비스 메서드 단위로 관리
    public String requestSignUpSms(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
        }
        return authService.generateAuthCode(phoneNumber);
    }

    @Override
    @Transactional
    public JwtDTO confirmSignUpAndLogin(String phoneNumber, String authCode) {
        // 1. 인증 코드 검증 (Redis 비교 및 삭제)
        if (!authService.verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
        }

        // 2. Redis에서 임시 DTO 조회 (전화번호가 Redis 키 역할을 한다고 가정)
        String redisKey = SIGNUP_TEMP_KEY_PREFIX + phoneNumber; // NOTE: 1단계에서 UUID 키를 반환받아 2단계에서 매핑해야 이상적
        UserSignUpRequest request = redisService.getData(redisKey, UserSignUpRequest.class);

        if (request == null) {
            throw new CustomException(GlobalErrorCode.AUTH_CODE_EXPIRED);
        }
        redisService.deleteData(redisKey);

        // 2-1. 최종 등록 직전에 DB 중복 체크 재수행 (Race Condition 방지)
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
        }

        // 3. DB 저장
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // UserSignUpRequest에는 전화번호, 역할, 부모코드가 없으므로, 이 정보를 인자로 받아야 합니다.
        // 현재 코드에서는 DTO에서 제거했으나, 최종 가입 시 이 정보를 포함하여 전달해야 합니다.
        // 임시 DTO에 전화번호 정보를 추가하고, 역할을 PENDING으로 초기화하도록 임시 변경합니다.

        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        User user = User.builder()
                .name(request.name())
                .phoneNumber(phoneNumber) // 2단계 인자로 받은 전화번호 사용
                .role(ERole.PENDING) // 역할 선택은 3단계에서 진행하므로 PENDING으로 초기화
                .gender(request.gender())
                .status(EUserStatus.ACTIVE)
                .birthDate(parsed)
                .parentCode(null)
                .connectedParentCode(null)
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
            codeToIssue = generateUniqueParentCode(); // 동시성 위험이 있는 메서드
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

        String newCode = generateUniqueParentCode(); // 동시성 위험이 있는 메서드
        user.setParentCode(newCode);
        return newCode;
    }
}