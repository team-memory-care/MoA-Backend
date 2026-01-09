package com.example.moabackend.domain.notification.entity;

import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToOne(fetch = FetchType.LAZY)
    Report report;

    @Column
    String title;

    @Column
    String body;

    @Column
    Boolean isRead = false;

    @Builder
    public Notification(User user, Report report, String title, String body) {
        this.user = user;
        this.report = report;
        this.title = title;
        this.body = body;
    }

    public void setIsReadTrue() {
        this.isRead = true;
    }
}
