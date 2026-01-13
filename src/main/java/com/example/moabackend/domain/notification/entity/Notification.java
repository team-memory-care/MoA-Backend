package com.example.moabackend.domain.notification.entity;

import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Column
    @CreationTimestamp
    LocalDateTime dateTime;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return this.id != null && Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
