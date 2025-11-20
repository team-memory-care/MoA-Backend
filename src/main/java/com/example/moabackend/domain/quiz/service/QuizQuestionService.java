package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.dto.res.question.QuizQuestionDto;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;

import java.util.List;

public interface QuizQuestionService {
    List<QuizQuestionDto> getTodayQuizSet(Long userId);

    List<QuizQuestionDto> getQuizSetByType(Long userId, EQuizType type);
}
