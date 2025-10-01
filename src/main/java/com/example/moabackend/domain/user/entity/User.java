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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthDate", nullable = false)
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

    @Builder
    public User(String name, LocalDate birthDate, String phoneNumber, ERole role, EUserStatus status, EUserGender gender) {
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.gender = gender;
    }
}
