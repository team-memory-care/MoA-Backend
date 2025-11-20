package com.example.moabackend.global.token.dto.req;

public record LogoutRequestDto(
        String accessToken,
        String refreshToken
) {}