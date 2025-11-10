package com.example.moabackend.domain.quiz.converter;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.res.*;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizConverter {

    private final ObjectMapper objectMapper;

    /**
     * DB에서 조회된 QuizQuestion 엔티티를 EQuizType에 맞는 다형성 DTO로 변환합니다.
     *
     * @param entity DB에서 조회된 QuizQuestion 엔티티
     * @return 해당 유형의 QuizQuestionDto (Persistence, Memory 등)
     */
    public QuizQuestionDto toDto(QuizQuestion entity) {
        EQuizType type = entity.getType();
        String detailJson = entity.getDetailData();

        if (detailJson == null || detailJson.trim().isEmpty()) {
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }

        try {
            // 1. 유형에 따라 detailData JSON을 파싱하여 DTO의 특화 필드에 매핑
            return switch (type) {
                case PERSISTENCE -> createPersistenceDto(entity, detailJson);
                case MEMORY -> createMemoryDto(entity, detailJson);
                case LINGUISTIC -> createLinguisticDto(entity, detailJson);
                case ATTENTION -> createAttentionDto(entity, detailJson);
                case SPACETIME -> createSpacetimeDto(entity, detailJson);

                default -> throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
            };
        } catch (JsonProcessingException e) {
            // JSON 파싱 실패는 심각한 DB 데이터 오류를 의미
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }
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

    // 1. PERSISTENCE (지남력) - 객관식 옵션 파싱
    private PersistenceQuizQuestionDto createPersistenceDto(QuizQuestion entity, String detailJson) throws JsonProcessingException {
        OptionsWrapper wrapper = objectMapper.readValue(detailJson, OptionsWrapper.class);
        List<String> options = wrapper.options();

        return new PersistenceQuizQuestionDto(
                entity.getId(), entity.getType(), entity.getQuestionFormat(), entity.getQuestionContent(), options
        );
    }

    // 2. LINGUISTIC (언어능력)
    private LinguisticQuizQuestionDto createLinguisticDto(QuizQuestion entity, String detailJson) throws JsonProcessingException {
        OptionsWrapper wrapper = objectMapper.readValue(detailJson, OptionsWrapper.class);
        List<String> options = wrapper.options();

        return new LinguisticQuizQuestionDto(
                entity.getId(), entity.getType(), entity.getQuestionFormat(), entity.getQuestionContent(),
                options
        );
    }

    // 3. MEMORY (기억력/음성 입력)
    private MemoryQuizQuestionDto createMemoryDto(QuizQuestion entity, String detailJson) throws JsonProcessingException {
        MemoryDetailDto detail = objectMapper.readValue(detailJson, MemoryDetailDto.class);

        return new MemoryQuizQuestionDto(
                entity.getId(), entity.getType(), entity.getQuestionFormat(), entity.getQuestionContent(),
                detail.inputMethod(), detail.requiredSequenceType()
        );
    }

    // 4. ATTENTION (주의력/계산)
    private AttentionQuizQuestionDto createAttentionDto(QuizQuestion entity, String detailJson) throws JsonProcessingException {
        AttentionDetailDto detail = objectMapper.readValue(detailJson, AttentionDetailDto.class);

        return new AttentionQuizQuestionDto(
                entity.getId(), entity.getType(), entity.getQuestionFormat(), entity.getQuestionContent(),
                detail.expression(), detail.inputType()
        );
    }

    // 5. SPACETIME (시공간)
    private SpacetimeQuizQuestionDto createSpacetimeDto(QuizQuestion entity, String detailJson) throws JsonProcessingException {
        SpacetimeDetailDto detail = objectMapper.readValue(detailJson, SpacetimeDetailDto.class);

        return new SpacetimeQuizQuestionDto(
                entity.getId(), entity.getType(), entity.getQuestionFormat(), entity.getQuestionContent(),
                detail.imageOptionsUrl()
        );
    }

    /**
     * PERSISTENCE, LINGUISTIC 처럼 {"options": [...]} 구조를 파싱하기 위한 Wrapper
     */
    private record OptionsWrapper(
            List<String> options
    ) {
    }

    private record MemoryDetailDto(
            String inputMethod,
            String requiredSequenceType
    ) {
    }

    private record AttentionDetailDto(
            String expression,
            String inputType
    ) {
    }

    private record SpacetimeDetailDto(
            List<String> imageOptionsUrl
    ) {
    }
}