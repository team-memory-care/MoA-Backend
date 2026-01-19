package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.dto.req.UserRegisterRequestDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    // --- [회원가입 관련 (Onboarding Flow)] ---

    /**
     * 1. 인증 번호 발송
     */
    @Override
    @Transactional
    public String requestSignUpSms(String phoneNumber) {
        String resolvedNumber = resolveTestNumber(phoneNumber);
        User existUser = userRepository.findByPhoneNumber(resolvedNumber).orElse(null);

        if (existUser != null && existUser.getStatus() == EUserStatus.ACTIVE) {
            throw new CustomException(UserErrorCode.USER_ALREADY_EXISTS);
        }

        return authService.generateSignUpAuthCode(phoneNumber);
    }

    private static final java.util.regex.Pattern NON_DIGIT_PATTERN = java.util.regex.Pattern.compile("[^0-9]");
    private static final String TEST_ACCOUNT_NUMBER = "821035477120";

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
     * 2. 회원가입 완료 및 로그인
     */
    @Override
    @Transactional
    public JwtDTO confirmSignUpAndLogin(UserRegisterRequestDto request) {
        if (!authService.verifyAuthCode(request.phoneNumber(), request.authCode())) {
            throw new CustomException(UserErrorCode.INVALID_AUTH_CODE);
        }

        String resolvedNumber = resolveTestNumber(request.phoneNumber());
        User existUser = userRepository.findByPhoneNumber(resolvedNumber).orElse(null);

        if (existUser != null) {
            if (existUser.getStatus() == EUserStatus.WITHDRAWN) {
                return reRegisterUser(existUser, request);
            }

            if (existUser.getStatus() == EUserStatus.ACTIVE) {
                log.info("Existing active user login: {}", request.phoneNumber());
                return authService.generateTokensForUser(existUser);
            }
            throw new CustomException(UserErrorCode.USER_STATUS_INVALID);
        }
        return createNewUser(request, resolvedNumber);
    }

    /**
     * 역할 선택: 부모(역할 변경 가능)
     */
    @Override
    @Transactional
    public ParentUserResponseDto selectParentRole(Long userId) {
        User user = getUserOrThrow(userId);

        validateCanSelectParentRole(user);

        String codeToIssue = generateUniqueParentCode();
        user.completeRoleSelection(ERole.PARENT, codeToIssue);

        return ParentUserResponseDto.from(user);
    }

    /**
     * 부모 코드 검증 (온보딩_2)
     */
    @Override
    public ChildUserResponseDto.LinkedParentResponseDto verifyParentCode(Long userId, String parentCode) {
        User parentUser = userRepository.findByParentCode(parentCode)
                .orElseThrow(() -> new CustomException(UserErrorCode.INVALID_PARENT_CODE));

        if (parentUser.getId().equals(userId)) {
            log.warn("[Security] User {} tried to verify their own parent code", userId);
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        log.info("[Verify] User {} verified parent code for parent: {}", userId, parentUser.getId());
        return ChildUserResponseDto.LinkedParentResponseDto.from(parentUser);
    }

    /**
     * 부모 자녀 최종 연결 (온보딩_3)
     */
    @Override
    @Transactional
    public void linkParent(Long userId, Long parentId) {
        User user = getUserOrThrow(userId);
        User parentUser = userRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() != ERole.PENDING && user.getRole() != ERole.CHILD) {
            throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
        }

        if (user.getId().equals(parentUser.getId())) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        user.addParent(parentUser);
        user.completeRoleSelection(ERole.CHILD, null);
    }

    // --- [사용자 정보 및 상태 관리] ---

    @Override
    public UserResponseDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));
        return UserResponseDto.from(user);
    }

    /**
     * 부모 정보 단일 조회
     */
    @Override
    public ChildUserResponseDto.LinkedParentResponseDto getParentInfoById(Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return ChildUserResponseDto.LinkedParentResponseDto.from(parent);
    }

    @Override
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = getUserOrThrow(userId);
        user.updateFcmToken(fcmToken);
    }

    /**
     * 부모 코드 발급
     */
    @Override
    @Transactional
    public String issueParentCode(Long userId) {
        User user = getUserOrThrow(userId);

        if (user.getRole() != ERole.PARENT) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        if (user.getParentCode() != null) {
            throw new CustomException(UserErrorCode.PARENT_CODE_ALREADY_EXISTS);
        }

        String newCode = generateUniqueParentCode();
        user.setParentCode(newCode);

        return newCode;
    }

    /**
     * 부모 코드 조회
     */
    @Override
    public String getParentCode(Long userId) {
        User user = getUserOrThrow(userId);

        if (user.getRole() != ERole.PARENT) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        String parentCode = user.getParentCode();
        if (parentCode == null) {
            throw new CustomException(UserErrorCode.PARENT_CODE_NOT_FOUND);
        }
        return parentCode;
    }

    @Override
    public List<ChildUserResponseDto.LinkedParentResponseDto> getMyParents(Long userId) {
        User user = userRepository.findWithParentsById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (user.getRole() != ERole.CHILD) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        return user.getParents().stream()
                .map(ChildUserResponseDto.LinkedParentResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public void disconnectParent(Long userId, Long parentId) {
        User user = userRepository.findWithParentsById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (user.getRole() != ERole.CHILD) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        boolean removed = user.getParents().removeIf(parent -> parent.getId().equals(parentId));

        if (!removed) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND);
        }
    }

    // --- [Private Helper Methods] ---

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

    private JwtDTO reRegisterUser(User user, UserRegisterRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        user.reRegister(request.name(), parsed, request.gender());
        return authService.generateTokensForUser(user);
    }

    private JwtDTO createNewUser(UserRegisterRequestDto request, String resolvedNumber) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        User user = User.builder()
                .name(request.name())
                .phoneNumber(resolvedNumber)
                .role(ERole.PENDING)
                .gender(request.gender())
                .status(EUserStatus.ACTIVE)
                .birthDate(parsed)
                .parentCode(null)
                .build();

        User savedUser = userRepository.save(user);
        return authService.generateTokensForUser(savedUser);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));
    }

    private void validateCanSelectParentRole(User user) {
        // 1. 관리자 권한 체크
        if (user.getRole() == ERole.ADMIN) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        // 2. 이미 보호자인 경우, 연결된 자녀가 있는지 확인
        if (user.getRole() == ERole.PARENT) {
            List<User> linkedChildren = userRepository.findAllByParents_Id(user.getId());
            if (!linkedChildren.isEmpty()) {
                log.warn("[Validation] User {} already has {} linked children. Re-selection denied.",
                        user.getId(), linkedChildren.size());
                throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
            }
        }

        // 3. 자녀(CHILD) 역할인 경우 재선택 허용 여부
        // 만약 자녀가 보호자로 바꾸고 싶다면, 먼저 연결된 부모 관계를 끊어야 함
        if (user.getRole() == ERole.CHILD) {
            if (user.getParents() != null && !user.getParents().isEmpty()) {
                throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
            }
        }
    }
}