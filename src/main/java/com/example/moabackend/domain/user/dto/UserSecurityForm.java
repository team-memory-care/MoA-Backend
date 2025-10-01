package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserStatus;

public record UserSecurityForm(
        Long id,
        ERole role,
        EUserStatus status
) {
    public Long getId() {
        return id;
    }
    public ERole getRole() {
        return role;
    }
    public EUserStatus getStatus() {
        return status;
    }

    public static UserSecurityForm from(User user) {
        return new UserSecurityForm(
                user.getId(),
                user.getRole(),
                user.getStatus()
        );
    }
}