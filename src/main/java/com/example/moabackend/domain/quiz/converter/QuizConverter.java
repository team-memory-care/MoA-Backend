package com.example.moabackend.domain.quiz.converter;

import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class QuizConverter {
    public QuizResult toEntity(User user, QuizSaveRequestDto dto) {
        return QuizResult.builder()
                .user(user)
                .totalNumber(dto.totalNumber())
                .correctNumber(dto.correctNumber())
                .date(LocalDate.now())
                .type(dto.type())
                .build();
    }
}
