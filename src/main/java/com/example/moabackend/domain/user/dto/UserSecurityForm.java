package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserStatus;

public record UserSecurityForm(
        Long id,
        ERole role,
        EUserStatus status
) {
    public static UserSecurityForm from(User user) {
        return new UserSecurityForm(
                user.getId(),
                user.getRole(),
                user.getStatus()
        );
    }
}