package com.example.moabackend.domain.quiz.entity.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EQuizType {
    PERSISTENCE("지남력"),
    MEMORY("기억력"),
    LINGUISTIC("언어능력"),
    ATTENTION("주의력"),
    SPACETIME("시공간");

    private final String value;
}
