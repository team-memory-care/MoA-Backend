package com.example.moabackend.domain.user.repository;

import com.example.moabackend.domain.user.dto.UserSecurityForm;
import com.example.moabackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<UserSecurityForm> findUserSecurityFormByPhoneNumber(String phoneNumber);

    Optional<UserSecurityForm> findUserSecurityFormById(Long id);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByParentCode(String parentCode);

    @EntityGraph(attributePaths = {"parents"})
    Optional<User> findWithParentsById(Long id);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByParentCode(String parentCode);

    List<User> findAllByParents_Id(Long parentId);

    List<User> findParentUserIdsById(Long id);
}


