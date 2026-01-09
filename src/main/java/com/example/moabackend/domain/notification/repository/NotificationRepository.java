package com.example.moabackend.domain.notification.repository;

import com.example.moabackend.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUser_Id(Long userId);

    @Query("""
                    select count(n)
                    from Notification n
                    where n.user.id = :userId
                    and n.isRead = false
            """)
    int findNotReadNotification(@Param("userId") Long userId);
}
