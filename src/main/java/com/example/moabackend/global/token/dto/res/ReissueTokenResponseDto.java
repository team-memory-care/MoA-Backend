package com.example.moabackend.global.token.dto.res;

import com.example.moabackend.domain.user.entity.type.ERole;

public record ReissueTokenResponseDto(
        String accessToken,
        String refreshToken,
        ERole role) {
    public static ReissueTokenResponseDto from(String accessToken, String refreshToken, ERole role) {
        return new ReissueTokenResponseDto(accessToken, refreshToken, role);
    }
}