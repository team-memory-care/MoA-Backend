package com.example.moabackend.global.security.info;

import com.example.moabackend.domain.user.dto.UserSecurityForm;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Builder
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {
    private final Long userId;
    private final String password;
    private final ERole role;
    private final EUserStatus status;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UserSecurityForm securityForm) {
        return UserPrincipal.builder()
                .userId(securityForm.getId())
                .role(securityForm.getRole())
                .status(securityForm.getStatus())
                .authorities(Collections.singleton(new SimpleGrantedAuthority(securityForm.getRole().getSecurityRole())))
                .build();
    }

    public static UserPrincipal create(
            UserSecurityForm securityForm,
            Map<String, Object> attributes
    ) {
        return UserPrincipal.builder()
                .userId(securityForm.getId())
                .role(securityForm.getRole())
                .attributes(attributes)
                .status(securityForm.getStatus())
                .authorities(Collections.singleton(
                        new SimpleGrantedAuthority(securityForm.getRole().getSecurityRole()))
                )
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.userId.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
