package com.example.moabackend.domain.user.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ERole {
    PARENT("PARENT", "ROLE_PARENT"),
    CHILD("CHILD", "ROLE_CHILD"),
    PENDING("PENDING", "ROLE_PENDING"),
    ADMIN("ADMIN", "ROLE_ADMIN");

    private final String role;
    private final String securityRole;
}
