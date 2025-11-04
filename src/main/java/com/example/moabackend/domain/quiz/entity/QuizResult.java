package com.example.moabackend.domain.quiz.entity;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Integer totalNumber;

    @Column
    private Integer correctNumber;

    @Column
    @Enumerated(EnumType.STRING)
    private EQuizType type;

    @Column
    private LocalDate date;

    @Builder
    public QuizResult(User user, Integer totalNumber, Integer correctNumber, EQuizType type, LocalDate date) {
        this.user = user;
        this.totalNumber = totalNumber;
        this.correctNumber = correctNumber;
        this.type = type;
        this.date = date;
    }

    public void updateCorrectNumber(int newCorrectNumber) {
        this.correctNumber = newCorrectNumber;
    }
}
