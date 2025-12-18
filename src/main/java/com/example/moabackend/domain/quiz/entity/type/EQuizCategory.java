package com.example.moabackend.domain.quiz.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EQuizCategory {
    TODAY("오늘의 퀴즈"),
    PRACTICE("유형별 학습");

    private final String value;
}
