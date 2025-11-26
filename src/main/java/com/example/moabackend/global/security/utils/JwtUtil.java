package com.example.moabackend.global.security.utils;

import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.constant.Constants;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.dto.JwtDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration}")
    @Getter
    private Integer accessExpiration;

    @Value("${jwt.refresh-token.expiration}")
    @Getter
    private Integer refreshExpiration;

    private Key key;

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(Long id, ERole role, Integer expiration) {
        Claims claims = Jwts.claims();
        claims.put(Constants.CLAIM_USER_ID, id);
        if (role != null)
            claims.put(Constants.CLAIM_USER_ROLE, role);

        return Jwts.builder()
                .setHeaderParam(Header.JWT_TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis())) //현재 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setId(UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }

    public JwtDTO generateTokens(Long id, ERole role) {
        return JwtDTO.of(
                generateToken(id, role, accessExpiration),
                generateToken(id, role, refreshExpiration),
                role
        );
    }

    public Long validateRefreshToken(final String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
            log.info("[JWT] Claims = {}", claims);

            Long userId = claims.get(Constants.CLAIM_USER_ID, Long.class);
            log.info("[JWT] Extracted userId = {}", userId);

            return claims.get(Constants.CLAIM_USER_ID, Long.class);
        } catch (ExpiredJwtException e) {
            log.info("[JWT] Refresh token expired: {}", refreshToken, e);
            throw new CustomException(GlobalErrorCode.EXPIRED_TOKEN_ERROR);
        } catch (Exception e) {
            log.info("[JWT] Invalid refresh token: {}", refreshToken, e);

            throw new CustomException(GlobalErrorCode.INVALID_TOKEN_ERROR);
        }
    }

    public String getJti(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getId();
    }

    public long getAccessTokenRemainingMillis(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Date expiration = claims.getExpiration(); // 만료 시간
        long now = System.currentTimeMillis();

        return expiration.getTime() - now; // 남은 밀리초 반환
    }
}