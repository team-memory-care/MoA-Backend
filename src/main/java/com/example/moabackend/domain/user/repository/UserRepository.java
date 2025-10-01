package com.example.moabackend.domain.user.repository;

import com.example.moabackend.domain.user.dto.UserSecurityForm;
import com.example.moabackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.id as id, u.role as role, u.status as status from User u where u.phoneNumber = :phoneNumber")
    Optional<UserSecurityForm> findUserSecurityFormByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("select u.id as id, u.role as role, u.status as status from User u where u.id = :id")
    Optional<UserSecurityForm> findUserSecurityFormById(@Param("id") Long id);
}
