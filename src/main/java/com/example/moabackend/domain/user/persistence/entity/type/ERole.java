package com.example.moabackend.domain.user.persistence.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ERole {
    GUEST("PARENT", "ROLE_PARENT"),
    USER("USER", "ROLE_USER"),
    ADMIN("ADMIN", "ROLE_ADMIN");

    private final String role;
    private final String securityRole;
}
