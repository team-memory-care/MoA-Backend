package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.token.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisService redisService;

    @Override
    public void preSignup(UserSignUpRequest request) {
        // 회원정보 Redis에 임시 저장 (TTL 5분)
        redisService.setData("preuser:" + request.phoneNumber(), request, 5);
    }

    @Override
    public UserResponseDto confirmSignup(String phoneNumber) {
        // 인증 여부 확인
        Boolean verified = redisService.getData("verified:" + phoneNumber, Boolean.class);
        if (verified == null || !verified) {
            throw new CustomException(GlobalErrorCode.INVALID_AUTH_CODE);
        }

        // 임시 저장된 사용자 정보 가져오기
        UserSignUpRequest request = redisService.getData("preuser:" + phoneNumber, UserSignUpRequest.class);
        if (request == null) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND_USER);
        }

        // User 엔티티 저장
        LocalDate parseBirthDate = LocalDate.parse(request.birthDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));

        User user = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .role(request.role())
                .gender(request.gender())
                .status(EUserStatus.ACTIVE)
                .birthDate(parseBirthDate)
                .build();

        User savedUser = userRepository.save(user);

        // 사용 후 redis 삭제
        redisService.deleteData("verified:" + phoneNumber);
        redisService.deleteData("preuser:" + phoneNumber);

        return UserResponseDto.from(savedUser);
    }
}