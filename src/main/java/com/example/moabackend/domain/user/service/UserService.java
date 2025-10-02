package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;

public interface UserService {
    // 1단계: 회원 개인정보 임시 저장
    void preSignup(UserSignUpRequest request);

    // 4단계: 최종 회원가입
    UserResponseDto confirmSignup(String phoneNumber);
}
