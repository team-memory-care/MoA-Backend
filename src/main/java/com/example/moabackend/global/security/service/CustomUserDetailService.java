package com.example.moabackend.global.security.service;

import com.example.moabackend.domain.user.dto.UserSecurityForm;
import com.example.moabackend.domain.user.exception.UserErrorCode;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.info.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserPrincipal loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {

        UserSecurityForm userSecurityForm = userRepository.findUserSecurityFormByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));
        log.info(("아이디 기반 조회 성공"));

        return UserPrincipal.create(userSecurityForm);
    }

    public UserPrincipal loadUserById(Long id) {
        UserSecurityForm userSecurityForm = userRepository.findUserSecurityFormById(id)
                .orElseThrow(() -> CustomException.type(UserErrorCode.USER_NOT_FOUND));
        log.info("user id 기반 조회 성공");

        return UserPrincipal.create(userSecurityForm);
    }
}
