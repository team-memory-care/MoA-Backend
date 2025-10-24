package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.token.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    // NOTE: RedisService는 현재 AuthService에서만 사용되므로, 이 필드는 제거하는 것이 권장됩니다.
    private final RedisService redisService;

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
     * 회원가입 API: 사용자 정보를 DB에 PENDING/INACTIVE 상태로 저장합니다.
     */
    @Override
    @Transactional
    public UserResponseDto signUp(UserSignUpRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new CustomException(GlobalErrorCode.ALREADY_EXISTS);
        }

        // DTO의 yyyyMMdd -> LocalDate 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsed = LocalDate.parse(request.birthDate(), formatter);

        // User 엔티티 생성 및 DB 저장 (초기 상태 설정)
        User user = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .role(ERole.PENDING)
                .gender(request.gender())
                .status(EUserStatus.INACTIVE)
                .birthDate(parsed)
                .parentCode(null)
                .connectedParentCode(null)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);
    }

    /**
     * 사용자 역할 선택 API: 미선택(PENDING) 상태에서 역할 확정 및 부모-자녀 연결을 수행합니다.
     */
    @Override
    @Transactional
    public UserResponseDto selectRoleAndLinkParent(Long userId, ERole role, String parentCode){
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        // 역할 선택은 최초 1번만 허용
        if(user.getRole() != ERole.PENDING){
            throw new CustomException(GlobalErrorCode.ALREADY_ROLE_SELECTED);
        }

        String codeToIssue = null;
        String codeToConnect = null;

        if(role==ERole.PARENT){
            codeToIssue = generateUniqueParentCode();
        } else if(role==ERole.CHILD){
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
    public String issueOrGetParentCode(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND_USER));

        // 부모 역할이 아닌 경우 접근 거부
        if(user.getRole() != ERole.PARENT){
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        // 코드가 이미 존재하면 조회
        if(user.getParentCode()!=null){
            return user.getParentCode();
        }

        // 코드가 없으면 새로 발급 및 저장
        String newCode = generateUniqueParentCode();
        user.setParentCode(newCode);
        return newCode;
    }
}