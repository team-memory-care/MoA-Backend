package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.dto.req.UserRegisterRequestDto;
import com.example.moabackend.domain.user.dto.res.ChildUserResponseDto;
import com.example.moabackend.domain.user.dto.res.ParentUserResponseDto;
import com.example.moabackend.domain.user.dto.res.UserResponseDto;
import com.example.moabackend.global.security.dto.JwtDTO;

import java.util.List;

public interface UserService {

    // --- [회원가입 관련 (Onboarding Flow)] ---

    /**
     * 1. 인증 코드 발송
     * 입력받은 전화번호로 SMS 인증 코드를 전송합니다.
     */
    String requestSignUpSms(String phoneNumber);

    /**
     * 2. 회원가입 완료 및 로그인
     * 인증 코드 검증 성공 시:
     * - 신규 유저: 회원 정보 생성 후 토큰 발급
     * - 기존 유저(ACTIVE): 즉시 로그인 처리 (토큰 발급)
     * - 기존 유저(WITHDRAWN): 회원 정보 갱신(재가입) 후 토큰 발급
     */
    JwtDTO confirmSignUpAndLogin(UserRegisterRequestDto request);

    /**
     * 역할 선택: 부모 (Parent)
     * 사용자의 역할을 '부모'로 확정하고, 자녀 연결을 위한 고유 코드를 생성합니다.
     */
    ParentUserResponseDto selectParentRole(Long userId);

    /**
     * 부모 코드 검증 (온보딩_2)
     * 실제 연결을 수행하지 않고, 입력된 코드로 부모의 존재 여부와 기본 정보를 반환합니다.
     */
    ChildUserResponseDto.LinkedParentResponseDto verifyParentCode(Long userId, String parentCode);

    /**
     * 부모 자녀 최종 연결 (온보딩_3)
     * 자녀의 역할을 확정하고 특정 부모(ID 기반)와 관계를 생성합니다.
     */
    ChildUserResponseDto linkParent(Long userId, Long parentId);


    // --- [사용자 정보 및 상태 관리] ---

    /**
     * 사용자 정보 조회
     */
    UserResponseDto findUserById(Long userId);

    /**
     * 부모 정보 단일 조회 (온보딩_3)
     * 부모 ID를 기반으로 해당 부모의 정보를 조회합니다.
     */
    ChildUserResponseDto.LinkedParentResponseDto getParentInfoById(Long parentId);

    /**
     * 부모 코드 신규 발급
     * 부모 사용자의 고유 연결 코드를 새로 발급합니다.
     */
    String issueParentCode(Long userId);

    /**
     * 부모 코드 조회
     * 부모 사용자의 고유 연결 코드를 조회합니다.
     */
    String getParentCode(Long userId);

    /**
     * 연결된 부모 목록 조회
     * 자녀 계정으로 연결된 부모들의 정보를 조회합니다.
     */
    List<ChildUserResponseDto.LinkedParentResponseDto> getMyParents(Long userId);

    /**
     * 연결된 부모 삭제
     * 자녀 계정에서 특정 부모와의 연결을 해제합니다.
     */
    void disconnectParent(Long userId, Long parentId);
}