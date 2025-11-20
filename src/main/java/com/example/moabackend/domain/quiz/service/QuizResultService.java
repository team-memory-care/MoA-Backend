package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.req.QuizSubmitRequestDto;
import com.example.moabackend.domain.quiz.dto.res.result.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.dto.res.result.QuizSubmitResponseDto;

import java.time.LocalDate;

public interface QuizResultService {
    QuizSubmitResponseDto submitAndScoreAnswer(Long userId, QuizSubmitRequestDto requestDto);

    void saveQuizResult(Long userId, QuizSaveRequestDto quizSaveRequestDto);

    Boolean hasCompletedAllQuiz(Long userId, LocalDate date);

    QuizRemainTypeResponseDto remainTypeQuiz(Long userId, LocalDate date);
}
