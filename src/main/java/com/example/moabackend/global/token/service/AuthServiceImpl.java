package com.example.moabackend.global.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StringRedisTemplate stringRedisTemplate;

    // 인증 코드 생성
    @Override
    public String generateAuthCode(String phoneNumber) {
        String code = String.format("%04d", new Random().nextInt(1000000));
        stringRedisTemplate.opsForValue()
                .set("auth: " + phoneNumber, code, 5, TimeUnit.MINUTES);
        return code;
    }

    // 인증 코드 검증
    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        String savedCode = stringRedisTemplate.opsForValue().get("auth: " + phoneNumber);

        if (savedCode != null && savedCode.equals(inputCode)) {
            stringRedisTemplate.delete("auth:" + phoneNumber);
            return true;
        }
        return false;
    }
}
