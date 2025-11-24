package com.example.moabackend.domain.quiz.converter;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.res.question.*;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class QuizConverter {

    private final ObjectMapper objectMapper;

    /**
     * DB에서 조회된 QuizQuestion 엔티티를 EQuizType에 맞는 다형성 DTO로 변환합니다.
     */
    public QuizQuestionDto toDto(QuizQuestion entity) {

        // 1. 기본 유효성 검사
        if (entity == null || entity.getType() == null) {
            throw new CustomException(GlobalErrorCode.INVALID_QUIZ_TYPE);
        }

        // 2. 데이터 존재 여부 체크
        if (entity.getDetailData() == null || entity.getDetailData().trim().isEmpty()) {
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }

        // 3. 각 DTO의 정적 팩터리 메서드에게 위임
        return switch (entity.getType()) {
            case PERSISTENCE -> PersistenceQuizQuestionDto.from(entity, objectMapper);
            case LINGUISTIC -> LinguisticQuizQuestionDto.from(entity, objectMapper);
            case MEMORY -> MemoryQuizQuestionDto.from(entity, objectMapper);
            case ATTENTION -> AttentionQuizQuestionDto.from(entity, objectMapper);
            case SPACETIME -> SpacetimeQuizQuestionDto.from(entity, objectMapper);

            default -> throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
        };
    }

    /**
     * 퀴즈 결과를 저장하기 위해 DTO를 QuizResult 엔티티로 변환합니다.
     */
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