package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.res.QuizRemainTypeResponseDto;

import java.time.LocalDate;

public interface QuizService {
    void saveQuizResult(Long userId, QuizSaveRequestDto quizSaveRequestDto);

    Boolean hasCompletedAllQuiz(Long userId, LocalDate date);

    QuizRemainTypeResponseDto remainTypeQuiz(Long userId, LocalDate date);
}
