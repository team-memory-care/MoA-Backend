package com.example.moabackend.global;

import com.example.moabackend.global.code.ErrorCode;
import com.example.moabackend.global.code.SuccessCode;
import lombok.Builder;
import org.springframework.http.ResponseEntity;

public record BaseResponse<T>(
        Boolean success,
        String message,
        T data
) {
    @Builder
    public BaseResponse {
    }

    public static <T> BaseResponse<T> success(final T data) {
        return BaseResponse.<T>builder()
                .success(Boolean.TRUE)
                .message("요청에 대해 정상적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success(SuccessCode s, T data) {
        return BaseResponse.<T>builder()
                .success(Boolean.TRUE)
                .message(s.getMessage())
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> fail(ErrorCode e) {
        return BaseResponse.<T>builder()
                .success(Boolean.FALSE)
                .message(e.getMessage())
                .data(null)
                .build();
    }

    public static <T> BaseResponse<T> fail(ErrorCode e, String errorMessage) {
        return BaseResponse.<T>builder()
                .success(Boolean.FALSE)
                .message(errorMessage)
                .data(null)
                .build();
    }

    public static <T> ResponseEntity<BaseResponse<T>> toResponseEntity(SuccessCode s, T data) {
        return ResponseEntity
                .status(s.getStatus())
                .body(BaseResponse.<T>builder()
                        .success(Boolean.TRUE)
                        .message(s.getMessage())
                        .data(data)
                        .build());
    }
}
