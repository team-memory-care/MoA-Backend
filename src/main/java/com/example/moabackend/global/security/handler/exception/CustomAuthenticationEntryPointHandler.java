package com.example.moabackend.global.security.handler.exception;

import com.example.moabackend.global.code.ErrorCode;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.security.info.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {
        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");
        if (errorCode == null) {
            AuthenticationResponse.makeFailureResponse(response, GlobalErrorCode.VALIDATION_ERROR);
            return;
        }
        AuthenticationResponse.makeFailureResponse(response, errorCode);
    }
}
