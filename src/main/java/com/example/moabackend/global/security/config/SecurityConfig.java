package com.example.moabackend.global.security.config;

import com.example.moabackend.global.security.filter.JwtAuthenticationFilter;
import com.example.moabackend.global.security.filter.JwtExceptionFilter;
import com.example.moabackend.global.security.handler.exception.CustomAccessDeniedHandler;
import com.example.moabackend.global.security.handler.exception.CustomAuthenticationEntryPointHandler;
import com.example.moabackend.global.security.handler.logout.CustomLogoutProcessHandler;
import com.example.moabackend.global.security.handler.logout.CustomLogoutResultHandler;
import com.example.moabackend.global.security.provider.JwtAuthenticationManager;
import com.example.moabackend.global.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomLogoutProcessHandler customLogoutProcessHandler;
    private final CustomLogoutResultHandler customLogoutResultHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationManager jwtAuthenticationManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(
                                        "/api/users/sms/request",
                                        "/api/users/signup",
                                        "/api/auth/sms/request",
                                        "/api/auth/login",
                                        "/api/auth/code/issue",
                                        "/api/users/select-role",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**")
                                .permitAll()
                                .requestMatchers("/api/users/select-role").hasAnyRole("PENDING", "CHILD", "PARENT", "ADMIN")

                                .requestMatchers("/api/**").authenticated() // hasAnyRole보다 authenticated()가 더 명확
                                .requestMatchers("/api/**").hasAnyRole("CHILD", "PARENT", "ADMIN")

                                .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(customLogoutProcessHandler)
                        .logoutSuccessHandler(customLogoutResultHandler)
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler) //권한 부족(403)
                        .authenticationEntryPoint(customAuthenticationEntryPointHandler) //인증 실패(401)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, jwtAuthenticationManager), LogoutFilter.class //JWT 토큰을 검사 → 인증 처리
                )
                .addFilterBefore(
                        new JwtExceptionFilter(), JwtAuthenticationFilter.class //JWT 인증 중 에러 핸들러
                )
                .getOrBuild();
    }
}