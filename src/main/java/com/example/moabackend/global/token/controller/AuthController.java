package com.example.moabackend.global.token.controller;

import com.example.moabackend.global.token.service.AuthServiceImpl;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber) {
        String code = authService.generateAuthCode(phoneNumber);
        return ResponseEntity.ok("인증 번호가 발송되었습니다. (테스트: " + code + ")");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String phoneNumber, @RequestParam String code) {
        boolean result = authService.verifyAuthCode(phoneNumber, code);
        return result
                ? ResponseEntity.ok("인증 성공") :
                ResponseEntity.badRequest().body("인증 실패");
    }
}
