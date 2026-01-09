package com.example.moabackend.domain.notification.repository;

import com.example.moabackend.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUser_Id(Long userId);
}
