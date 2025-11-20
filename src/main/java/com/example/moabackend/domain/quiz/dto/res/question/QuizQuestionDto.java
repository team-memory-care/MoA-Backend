package com.example.moabackend.domain.quiz.dto.res.question;

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
        @JsonSubTypes.Type(value = MemoryQuizQuestionDto.class, name = "MEMORY"),
        @JsonSubTypes.Type(value = LinguisticQuizQuestionDto.class, name = "LINGUISTIC"),
        @JsonSubTypes.Type(value = AttentionQuizQuestionDto.class, name = "ATTENTION"),
        @JsonSubTypes.Type(value = SpacetimeQuizQuestionDto.class, name = "SPACETIME")
})

public sealed interface QuizQuestionDto permits
        PersistenceQuizQuestionDto, LinguisticQuizQuestionDto, MemoryQuizQuestionDto
        , AttentionQuizQuestionDto, SpacetimeQuizQuestionDto {
    Long questionId();

    EQuizType quizType();

    String questionFormat();

    String questionContent();
}