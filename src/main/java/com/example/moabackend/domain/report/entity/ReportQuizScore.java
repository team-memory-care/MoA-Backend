package com.example.moabackend.domain.report.entity;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportQuizScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EQuizType type;

    @Column(nullable = false)
    private int correctNumber;

    @Column(nullable = false)
    private int totalNumber;

    @Builder
    public ReportQuizScore(Report report, EQuizType type, Integer correctNumber, Integer totalNumber) {
        this.report = report;
        this.type = type;
        this.correctNumber = correctNumber;
        this.totalNumber = totalNumber;
    }
}