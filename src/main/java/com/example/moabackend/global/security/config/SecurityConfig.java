package com.example.moabackend.global.security.config;

import com.example.moabackend.global.constant.Constants;
import com.example.moabackend.global.security.filter.JwtAuthenticationFilter;
import com.example.moabackend.global.security.filter.JwtExceptionFilter;
import com.example.moabackend.global.security.handler.exception.CustomAccessDeniedHandler;
import com.example.moabackend.global.security.handler.exception.CustomAuthenticationEntryPointHandler;
import com.example.moabackend.global.security.provider.JwtAuthenticationManager;
import com.example.moabackend.global.security.utils.JwtUtil;
import com.example.moabackend.global.token.service.AccessTokenDenyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final AccessTokenDenyService accessTokenDenyService;

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
                                .requestMatchers(Constants.NO_NEED_AUTH.toArray(String[]::new)).permitAll()
                                .requestMatchers("/api/v1/users/my-parents").hasRole("CHILD")
                                .requestMatchers("/api/v1/users/role/child").hasAnyRole("PENDING", "CHILD", "PARENT", "ADMIN") //

                                .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler) //권한 부족(403)
                        .authenticationEntryPoint(customAuthenticationEntryPointHandler) //인증 실패(401)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, jwtAuthenticationManager, accessTokenDenyService), UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        new JwtExceptionFilter(), JwtAuthenticationFilter.class //JWT 인증 중 에러 핸들러
                )
                .getOrBuild();
    }
}