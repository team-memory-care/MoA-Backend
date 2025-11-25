package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.req.UserRegisterRequestDto;
import com.example.moabackend.domain.user.dto.res.ChildUserResponseDto;
import com.example.moabackend.domain.user.dto.res.ParentUserResponseDto;
import com.example.moabackend.domain.user.dto.res.UserResponseDto;
import com.example.moabackend.global.security.dto.JwtDTO;

public interface UserService {

    /**
     * 1. 인증 코드 발송
     * 입력받은 전화번호로 SMS 인증 코드를 전송합니다.
     */
    String requestSignUpSms(String phoneNumber);

    /**
     * 2. 회원가입 완료 및 로그인
     * 인증 코드 검증 성공 시 회원 정보를 생성(또는 갱신)하고 JWT 토큰을 발급합니다.
     */
    JwtDTO confirmSignUpAndLogin(UserRegisterRequestDto request);

    /**
     * 역할 선택: 부모 (Parent)
     * 사용자의 역할을 '부모'로 확정하고, 자녀 연결을 위한 고유 코드를 생성합니다.
     */
    ParentUserResponseDto selectParentRole(Long userId);

    /**
     * 역할 선택: 자녀 (Child)
     * 사용자의 역할을 '자녀'로 확정하고, 입력받은 코드로 부모와 연결합니다.
     */
    ChildUserResponseDto selectChildRoleAndLinkParent(Long userId, String parentCode);

    /**
     * 부모 코드 신규 발급
     *부모 사용자의 고유 연결 코드를 새로 발급합니다. (POST 전용)
     */
    String issueParentCode(Long userId);

    /**
     * 부모 코드 조회
     * 부모 사용자의 고유 연결 코드를 조회합니다. (GET 전용)
     */
    String getParentCode(Long userId);

    // [사용자 정보 조회 API]
    UserResponseDto findUserById(Long userId);
}