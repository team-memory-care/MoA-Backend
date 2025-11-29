package com.example.moabackend.domain.report.entity;

import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EReportType type;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private String oneLineReview;

    @Column
    private Integer completeRate;

    @Column
    private Integer correctRate;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String strategy;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportQuizScore> reportQuizScores;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Advice> advices;

    @Builder
    public Report(User user, EReportType type, LocalDate date, String oneLineReview, int completeRate, int correctRate, String diagnosis,
                  String strategy, List<ReportQuizScore> reportQuizScores, List<Advice> advices) {
        this.user = user;
        this.type = type;
        this.date = date;
        this.oneLineReview = oneLineReview;
        this.completeRate = completeRate;
        this.correctRate = correctRate;
        this.diagnosis = diagnosis;
        this.strategy = strategy;
        this.reportQuizScores = reportQuizScores;
        this.advices = advices;
    }

    public void addReportQuizScore(List<ReportQuizScore> reportQuizScores) {
        this.reportQuizScores.addAll(reportQuizScores);
    }

    public void addAdvice(List<Advice> advice) {
        this.advices.addAll(advice);
    }
}
