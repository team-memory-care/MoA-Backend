package com.example.moabackend.domain.quiz.dto.res;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "quizType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersistenceQuizQuestionDto.class, name = "PERSISTENCE"),
        @JsonSubTypes.Type(value = LinguisticQuizQuestionDto.class, name = "LINGUISTIC"),
        @JsonSubTypes.Type(value = MemoryQuizQuestionDto.class, name = "MEMORY"),

        @JsonSubTypes.Type(value = AttentionQuizQuestionDto.class, name = "ATTENTION"),
        @JsonSubTypes.Type(value = SpacetimeQuizQuestionDto.class, name = "SPACETIME")
})
// sealed 키워드를 사용하여 구현 가능한 record를 제한
public sealed interface QuizQuestionDto permits
        PersistenceQuizQuestionDto, LinguisticQuizQuestionDto, MemoryQuizQuestionDto
        , AttentionQuizQuestionDto, SpacetimeQuizQuestionDto
{
    // 공통 필드에 대한 접근 메서드 정의 (record의 component 이름과 일치해야 Jackson이 매핑 가능)
    Long questionId();
    EQuizType quizType();
    String questionFormat();
    String questionContent();
}