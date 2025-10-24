package com.example.moabackend.domain.user.repository;

import com.example.moabackend.domain.user.dto.UserSecurityForm;
import com.example.moabackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<UserSecurityForm> findUserSecurityFormByPhoneNumber(String phoneNumber);

    Optional<UserSecurityForm> findUserSecurityFormById(Long id);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByParentCode(String parentCode);
}
