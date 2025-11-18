package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.req.UserSignUpRequestDto;
import com.example.moabackend.domain.user.dto.res.ChildUserResponseDto;
import com.example.moabackend.domain.user.dto.res.ParentUserResponseDto;
import com.example.moabackend.global.security.dto.JwtDTO;

public interface UserService {
    // [회원가입 1단계] 사용자 기본 정보를 Redis에 임시 저장
    void preSignUp(UserSignUpRequestDto request);

    // [회원가입 2단계-1] 전화번호 중복 체크 및 인증 코드 발송
    String requestSignUpSms(String phoneNumber);

    // [회원가입 2단계-2] 최종 회원가입 및 토큰 발행
    JwtDTO confirmSignUpAndLogin(String phoneNumber, String authCode);

    // [사용자 선택 API] 역할 확정 및 부모 코드 연결 (최초 1회)
    ParentUserResponseDto selectParentRole(Long userId);
    ChildUserResponseDto selectChildRoleAndLinkParent(Long userId, String parentCode);

    // [회원코드 생성 API] 부모 코드를 발급/조회
    String issueOrGetParentCode(Long userId);
}