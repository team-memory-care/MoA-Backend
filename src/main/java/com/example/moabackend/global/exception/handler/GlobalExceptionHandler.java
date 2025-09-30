package com.example.moabackend.global.exception.handler;

import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.code.ErrorCode;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Custom Exception 전용 ExceptionHandler
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Void>> customException(CustomException e) {

        ErrorCode errorCode = e.getErrorCode();
        log.warn("사용자 설정 예외: status: {}, message: {}", errorCode.getStatus(), errorCode.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(BaseResponse.fail(e.getErrorCode()));
    }

    /**
     * 요청 JSON 형식이 잘못된 경우
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return convert(GlobalErrorCode.BAD_JSON, e.getMessage());
    }

    /**
     * 요청 데이터 Validation 전용 ExceptionHandler (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return convert(GlobalErrorCode.VALIDATION_ERROR, extractErrorMessage(e.getFieldErrors()));
    }

    /**
     * 타입이 일치하지 않는 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String value = String.valueOf(e.getValue());
        String message = String.format("요청 파라미터 [%s]의 값 [%s]은 올바른 형식이 아닙니다.", name, value);
        return convert(GlobalErrorCode.TYPE_MISMATCH, message);
    }

    /**
     * 존재하지 않는 Endpoint 전용 ExceptionHandler
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoHandlerFound() {
        return convert(GlobalErrorCode.NOT_SUPPORTED_URI_ERROR);
    }

    /**
     * HTTP Request Method 오류 전용 ExceptionHandler
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotSupported() {
        return convert(GlobalErrorCode.NOT_SUPPORTED_METHOD_ERROR);
    }

    /**
     * 데이터베이스 제약 조건에 위배된 경우 (@Column)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(DataIntegrityViolationException e) {
        return convert(GlobalErrorCode.CONSTRAINT_VIOLATION, e.getMessage());
    }

    /**
     * 필수 헤더가 누락된 경우
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException e) {
        return convert(GlobalErrorCode.MISSING_REQUEST_HEADER, e.getMessage());
    }

    /**
     * 내부 서버 오류 전용 ExceptionHandler
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<Void>> handleAnyException(RuntimeException e, WebRequest request) {
        return convert(GlobalErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * 그 외 에러
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleAnyException(Exception e, WebRequest request) {
        return convert(GlobalErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private ResponseEntity<BaseResponse<Void>> convert(ErrorCode code) {
        return ResponseEntity
                .status(code.getStatus())
                .body(BaseResponse.fail(code));
    }

    private ResponseEntity<BaseResponse<Void>> convert(ErrorCode code, String message) {
        log.debug("Message: {}", message);
        return ResponseEntity
                .status(code.getStatus())
                .body(BaseResponse.fail(code, message));
    }

    private String extractErrorMessage(List<FieldError> fieldErrors) {
        if (fieldErrors.size() == 1) {
            return fieldErrors.get(0).getDefaultMessage();
        }

        StringBuilder buffer = new StringBuilder();
        for (FieldError error : fieldErrors) {
            buffer.append(error.getDefaultMessage()).append("\n");
        }
        return buffer.toString();
    }
}