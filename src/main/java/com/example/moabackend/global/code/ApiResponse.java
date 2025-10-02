package com.example.moabackend.global.code;

import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        boolean success,
        int status,
        String code,
        String message,
        T data
) {
    // 성공 응답
    public static <T> ResponseEntity<ApiResponse<T>> success(GlobalSuccessCode code, T data) {
        ApiResponse<T> response = new ApiResponse<>(
                true,
                code.getStatus().value(),
                code.getCode(),
                code.getMessage(),
                data
        );
        return ResponseEntity.status(code.getStatus()).body(response);
    }

    // 실패 응답
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode code) {
        ApiResponse<T> response = new ApiResponse<>(
                false,
                code.getStatus().value(),
                code.getCode(),
                code.getMessage(),
                null
        );
        return ResponseEntity.status(code.getStatus()).body(response);
    }

}
