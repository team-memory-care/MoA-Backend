package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.entity.type.ERole;

public interface UserService {
    // [회원가입 1단계] 사용자 정보를 Redis에 임시 저장하고 인증 코드를 발송
    String preSignUpAndSendCode(UserSignUpRequest request);

    // [회원가입 2단계] 인증 코드를 검증하고, 최종적으로 DB에 회원 등록
    UserResponseDto confirmSignUp(String phoneNumber, String authCode);

    // [사용자 선택 API] 역할 확정 및 부모 코드 연결 (최초 1회)
    UserResponseDto selectRoleAndLinkParent(Long userId, ERole role, String parentCode);

    // [회원코드 생성 API] 부모 코드를 발급/조회
    String issueOrGetParentCode(Long userId);
}
