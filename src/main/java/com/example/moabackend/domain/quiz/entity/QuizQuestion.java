package com.example.moabackend.domain.quiz.entity;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz_question")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공통 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EQuizType type;

    @Column(nullable = false, length = 500)
    private String questionContent;

    @Column(nullable = false, length = 100)
    private String questionFormat;

    @Column(nullable = false, length = 100)
    private String answer;

    @Column(name = "detail_data", columnDefinition = "jsonb")
    private String detailData;

    @Builder

    public QuizQuestion(Long id, EQuizType type, String questionContent, String questionFormat, String answer, String detailData) {
        this.id = id;
        this.type = type;
        this.questionContent = questionContent;
        this.questionFormat = questionFormat;
        this.answer = answer;
        this.detailData = detailData;
    }
}
