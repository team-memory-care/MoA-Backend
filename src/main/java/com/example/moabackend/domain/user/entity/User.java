package com.example.moabackend.domain.user.entity;

import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "role", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ERole role;

    @Column(name = "status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private EUserStatus status;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private EUserGender gender;

    // 부모 사용자에게 발급되는 4자리 개인 회원코드
    @Column(name = "parentCode", unique = true, nullable = true, length = 4)
    private String parentCode;

    // 자녀가 입력해서 부모랑 연결되는 코드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = true)
    private User parent;

    // 로그인 성공시 상태를 ACTIVE로 변경
    public void activate() {
        this.status = EUserStatus.ACTIVE;
    }

    // 역할 선택 시 역할/부모코드 최종 확정
    public void completeRoleSelection(ERole newRole, String codeToIssue, User parentUser) {
        this.role = newRole;
        this.parentCode = codeToIssue;
        this.parent = parentUser;
    }

    // 부모 코드 발급/업데이트 (parentCode는 한 번 발급되면 바뀌지 않아야 함)
    public void setParentCode(String code) {
        this.parentCode = code;
    }

    @Builder
    public User(String name, LocalDate birthDate, String phoneNumber, ERole role, EUserStatus status, EUserGender gender, String parentCode, User parent) {
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.gender = gender;
        this.parentCode = parentCode;
        this.parent = parent;
    }
}
