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

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ERole role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EUserStatus status;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private EUserGender gender;

    // 부모 사용자에게 발급되는 4자리 개인 회원코드
    @Column(name = "parentCode", unique = true, nullable = true, length = 4)
    private String parentCode;

    // 자녀가 입력해서 부모랑 연결되는 코드
    @Column(name = "connect_parent_code", nullable = true, length = 4)
    private String connectedParentCode;

    @Builder
    public User(String name, LocalDate birthDate, String phoneNumber, ERole role, EUserStatus status, EUserGender gender, String parentCode, String connectedParentCode) {
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.gender = gender;
        this.parentCode = parentCode;
        this.connectedParentCode = connectedParentCode;
    }
}
