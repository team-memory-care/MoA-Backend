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

    /**
     * 부모용 고유 코드(4자리) 생성
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
     * 1. 인증 번호 발송
     */
    @Override
    @Transactional
    public String requestSignUpSms(String phoneNumber) {
        // 탈퇴하지 않은 기존 회원이 있는지 확인
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(user -> {
            if (user.getStatus() != EUserStatus.WITHDRAWN) {
                throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
            }
        });
        return authService.generateSignUpAuthCode(phoneNumber);
    }

    /**
     * 2. 회원가입 완료 및 로그인
     */
    @Override
    @Transactional
    public JwtDTO confirmSignUpAndLogin(UserRegisterRequestDto request) {
        // 2-1. 인증 코드 검증
        if (!authService.verifyAuthCode(request.phoneNumber(), request.authCode())) {
            throw new CustomException(UserErrorCode.INVALID_AUTH_CODE);
        }

        // 2-2. 기존 회원(탈퇴 등) 확인 및 처리
        User existUser = userRepository.findByPhoneNumber(request.phoneNumber()).orElse(null);
        if (existUser != null) {
            if (existUser.getStatus() != EUserStatus.WITHDRAWN) {
                throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
            }
            return reRegisterUser(existUser, request);
        }

        // 2-3. 신규 회원 생성
        return createNewUser(request);
    }

    /**
     * 역할 선택: 부모
     */
    @Override
    @Transactional
    public ParentUserResponseDto selectParentRole(Long userId) {
        User user = getUserOrThrow(userId);
        validatePendingRole(user);

        String codeToIssue = generateUniqueParentCode();
        user.completeRoleSelection(ERole.PARENT, codeToIssue);

        return ParentUserResponseDto.from(user);
    }

    /**
     * 역할 선택: 자녀
     */
    @Override
    @Transactional
    public ChildUserResponseDto selectChildRoleAndLinkParent(Long userId, String parentCode) {
        User user = getUserOrThrow(userId);

        if (user.getRole() != ERole.PENDING && user.getRole() != ERole.CHILD) {
            throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
        }

        User parentUser = userRepository.findByParentCode(parentCode)
                .orElseThrow(() -> new CustomException(UserErrorCode.INVALID_PARENT_CODE));

        if (user.getId().equals(parentUser.getId())) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        user.addParent(parentUser);
        user.completeRoleSelection(ERole.CHILD, null);

        return ChildUserResponseDto.from(user);
    }

    /**
     * 부모 코드 발급
     */
    @Override
    @Transactional
    public String issueParentCode(Long userId) {
        User user = getUserOrThrow(userId);

        // [1] 권한 확인: 부모 역할이 아닌 경우
        if (user.getRole() != ERole.PARENT) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        // [2] 이미 발급된 경우 확인
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

        // [1] 권한 확인: 부모 역할이 아닌 경우
        if (user.getRole() != ERole.PARENT) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        String parentCode = user.getParentCode();

        // [2] 코드 존재 여부 확인
        if (parentCode == null) {
            throw new CustomException(UserErrorCode.PARENT_CODE_NOT_FOUND);
        }
        return parentCode;
    }

    @Override
    public UserResponseDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));
        return UserResponseDto.from(user);
    }

    @Override
    public List<ChildUserResponseDto.LinkedParentResponseDto> getMyParents(Long userId) {
        User user = userRepository.findWithParentsById(userId).orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

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
        User user = userRepository.findWithParentsById(userId).orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        if (user.getRole() != ERole.CHILD) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        boolean removed = user.getParents().removeIf(parent -> parent.getId().equals(parentId));

        if (!removed) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND);
        }
    }

    private JwtDTO reRegisterUser(User user, UserRegisterRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        user.reRegister(request.name(), parsed, request.gender());
        return authService.generateTokensForUser(user);
    }

    private JwtDTO createNewUser(UserRegisterRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        User user = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
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

    private void validatePendingRole(User user) {
        if (user.getRole() != ERole.PENDING) {
            throw new CustomException(UserErrorCode.ALREADY_ROLE_SELECTED);
        }
    }
}