package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.dto.req.UserSignUpRequestDto;
import com.example.moabackend.domain.user.dto.res.ChildUserResponseDto;
import com.example.moabackend.domain.user.dto.res.ParentUserResponseDto;
import com.example.moabackend.domain.user.dto.res.UserResponseDto;
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

    private static final long SIGNUP_TTL_MINUTES = 5;
    private static final String SIGNUP_TEMP_KEY_PREFIX = "signup:temp:";
    private final UserRepository userRepository;
    private final AuthService authService;
    private final RedisService redisService;

    /**
     * 중복되지 않는 4자리 부모 회원 코드를 생성합니다.
     * (UserRepository에 existsByParentCode(String) 메서드가 있다고 가정)
     */
    private String generateUniqueParentCode() {
        final int MAX_RETRIES = 5;
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!userRepository.existsByParentCode(code)) {
                return code;
            }
        }
        log.error("Failed to generate unique parent code after {} retries.", MAX_RETRIES);
        throw new CustomException(UserErrorCode.CODE_GENERATION_FAILED);
    }

    /**
     * [회원가입 1단계] 사용자 기본 정보를 Redis에 임시 저장합니다. 키: SIGNUP_TEMP_KEY_PREFIX + 전화번호
     */
    @Override
    @Transactional // 쓰기 작업 필요
    public void preSignUp(UserSignUpRequestDto request) {
        String redisKey = SIGNUP_TEMP_KEY_PREFIX + request.phoneNumber();
        redisService.setData(redisKey, request, SIGNUP_TTL_MINUTES);
    }

    /**
     * [회원가입 2단계-1] 전화번호 중복 체크 및 인증 코드 발송을 처리합니다.
     */
    @Override
    @Transactional(readOnly = false)
    public String requestSignUpSms(String phoneNumber) {
        // DB에 이미 존재하는지 확인 (회원가입이므로 없어야 함)
        // DB에 이미 존재하는지 확인 (회원가입이므로 없어야 함, 단 탈퇴 회원은 허용)
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(user -> {
            if (user.getStatus() != EUserStatus.WITHDRAWN) {
                throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
            }
        });
        // 회원가입용 인증 코드 발송 (AuthService에 분리된 로직 호출)
        return authService.generateSignUpAuthCode(phoneNumber);
    }

    /**
     * [회원가입 2단계-2] 인증 코드 검증 및 최종 회원가입/토큰 발행을 수행합니다.
     */
    @Override
    @Transactional // 쓰기 작업 필요
    public JwtDTO confirmSignUpAndLogin(String phoneNumber, String authCode) {
        if (!authService.verifyAuthCode(phoneNumber, authCode)) {
            throw new CustomException(UserErrorCode.INVALID_AUTH_CODE);
        }

        String redisKey = SIGNUP_TEMP_KEY_PREFIX + phoneNumber;
        UserSignUpRequestDto request = redisService.getData(redisKey, UserSignUpRequestDto.class);

        if (request == null) {
            throw new CustomException(UserErrorCode.AUTH_CODE_EXPIRED);
        }
        redisService.deleteData(redisKey);

        // 2-1. 최종 등록 직전 중복 체크 (Double Check, Race Condition 방지)
        User existUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null);

        if (existUser != null) {
            if (existUser.getStatus() != EUserStatus.WITHDRAWN) {
                throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

            existUser.reRegister(request.name(), parsed, request.gender());
            return authService.generateTokensForUser(existUser);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        // 3. DB 저장 (User 엔티티 생성)
        User user = User.builder()
                .name(request.name())
                .phoneNumber(phoneNumber)
                .role(ERole.PENDING) // 3단계에서 역할 선택하도록 PENDING 상태로 초기화
                .gender(request.gender())
                .status(EUserStatus.ACTIVE)
                .birthDate(parsed)
                .parentCode(null)
                .build();

        User savedUser = userRepository.save(user);
        return authService.generateTokensForUser(savedUser);
    }

    /**
     * 사용자 역할 선택 API: 미선택(PENDING) 상태에서 역할 확정 및 부모-자녀 연결을 수행합니다.
     * (수정: User 엔티티의 completeRoleSelection 시그니처와 정규화된 로직에 맞춤)
     */
    @Override
    @Transactional // 쓰기 작업 필요
    public ParentUserResponseDto selectParentRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (user.getRole() != ERole.PENDING) {
            throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
        }

        String codeToIssue = generateUniqueParentCode();

        user.completeRoleSelection(ERole.PARENT, codeToIssue, null);
        return ParentUserResponseDto.from(user);
    }

    @Override
    @Transactional // 쓰기 작업 필요
    public ChildUserResponseDto selectChildRoleAndLinkParent(Long userId, String parentCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (user.getRole() != ERole.PENDING) {
            throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
        }

        User parentUser = userRepository.findByParentCode(parentCode)
                .orElseThrow(() -> new CustomException(UserErrorCode.INVALID_PARENT_CODE));

        user.completeRoleSelection(ERole.CHILD, null, parentUser);
        return ChildUserResponseDto.from(user);
    }

    /**
     * 회원 코드 생성 API: 인증된 부모 사용자의 회원 코드를 조회하거나 발급합니다.
     */
    @Override
    @Transactional // 쓰기 작업 필요
    public String issueOrGetParentCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

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

    @Override
    public UserResponseDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));
        return UserResponseDto.from(user);
    }
}
