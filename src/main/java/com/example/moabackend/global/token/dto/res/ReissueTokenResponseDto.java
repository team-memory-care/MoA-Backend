package com.example.moabackend.global.token.dto.res;

public record ReissueTokenResponseDto(
        String accessToken
) {
    public static ReissueTokenResponseDto from(String accessToken){
        return new ReissueTokenResponseDto(accessToken);
    }
}