package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.example.moabackend.domain.user.entity.type.EUserStatus;

public record UserResponseDto(
        Long id,
        String name,
        String phoneNumber,
        ERole role,
        EUserGender gender,
        EUserStatus status
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getGender(),
                user.getStatus()
        );
    }
}
