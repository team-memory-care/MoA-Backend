package com.example.moabackend.global.security.info;

import com.example.moabackend.domain.user.entity.type.ERole;
import lombok.Builder;

@Builder
public record JwtUserInfo(Long userId, ERole role) {
    public static JwtUserInfo of(Long userId, ERole role){
        return JwtUserInfo.builder()
                .userId(userId)
                .role(role)
                .build();
    }
}
