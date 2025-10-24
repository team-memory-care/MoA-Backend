package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UserResponseDto(
        Long id,
        String name,
        String phoneNumber,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
        ERole role,
        EUserGender gender,
        EUserStatus status,
        String parentCode,
        String connectedParentCode
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getRole(),
                user.getGender(),
                user.getStatus(),
                user.getParentCode(),
                user.getConnectedParentCode()
        );
    }
}
