package com.example.moabackend.global.security.dto;

import com.example.moabackend.domain.user.entity.type.ERole;
import lombok.Builder;

@Builder
public record JwtDTO(
        String accessToken,
        String refreshToken,
        ERole role
) {
    public static JwtDTO of(String accessToken, String refreshToken, ERole role){
        return JwtDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }
}
