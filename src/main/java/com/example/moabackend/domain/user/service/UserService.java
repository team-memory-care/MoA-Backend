package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.type.ERole;

public interface UserService {
    // 1단계: 회원 개인정보 임시 저장
    // void preSignup(UserSignUpRequest request);

    // 4단계: 최종 회원가입
    //UserResponseDto confirmSignup(String phoneNumber);

    // [회원가입 API] 회원 정보 DB에 즉시 저장 (PENDING 상태)
    UserResponseDto signUp(UserSignUpRequest request);

    // [사용자 선택 API] 역할 확정 및 부모 코드 연결 (최초 1회)
    UserResponseDto selectRoleAndLinkParent(Long userId, ERole role, String parentCode);

    // [회원코드 생성 API] 부모 코드를 발급/조회
    String issueOrGetParentCode(Long userId);
}
