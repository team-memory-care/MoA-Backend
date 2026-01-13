package com.example.moabackend.domain.quiz.entity;

import com.example.moabackend.domain.quiz.entity.type.EQuizCategory;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "date", "type", "category"})
})
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int totalNumber;

    @Column(nullable = false)
    private int correctNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EQuizType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EQuizCategory category;

    @Column(nullable = false)
    private LocalDate date;

    @Builder
    public QuizResult(User user, int totalNumber, int correctNumber, EQuizType type, EQuizCategory category, LocalDate date) {
        this.user = user;
        this.totalNumber = totalNumber;
        this.correctNumber = correctNumber;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public void updateCorrectNumber(int newCorrectCount) {
        this.correctNumber = newCorrectCount;
    }

    public void updateTotalNumber(int newTotalCount) {
        this.totalNumber = newTotalCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizResult that)) return false;
        return this.id != null && Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
