package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserResponseDto signUp(UserSignUpRequest request) {

        // Redis에서 인증번호 조회
        String savedCode = stringRedisTemplate.opsForValue().get("auth: " + request.phoneNumber());

        if(savedCode==null || !savedCode.equals(request.authCode())){
            throw new IllegalArgumentException("전화번호 인증 실패: 인증 번호가 일치하지 않거나 만료되었습니다.");
        }

        // 인증 성공시 Redis에서 제거
        stringRedisTemplate.delete("auth: " + request.phoneNumber());

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
        return UserResponseDto.from(savedUser);
    }
}
