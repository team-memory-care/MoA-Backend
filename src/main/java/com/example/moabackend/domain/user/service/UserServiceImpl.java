package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto signUp(UserSignUpRequest request) {
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
        return UserResponseDto.from(user);
    }
}
