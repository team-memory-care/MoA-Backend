package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;

public interface UserService {
    UserResponseDto signUp(UserSignUpRequest request);
}
