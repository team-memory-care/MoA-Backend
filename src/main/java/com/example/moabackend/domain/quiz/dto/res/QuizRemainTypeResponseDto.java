package com.example.moabackend.domain.quiz.dto.res;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

import java.util.List;

public record QuizRemainTypeResponseDto(
        List<EQuizType> remainQuizTypeList
) {
    public static QuizRemainTypeResponseDto of(List<EQuizType> remainQuizTypeList) {
        return new QuizRemainTypeResponseDto(remainQuizTypeList);
    }
}
