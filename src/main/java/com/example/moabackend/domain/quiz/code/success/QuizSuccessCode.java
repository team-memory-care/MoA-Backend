package com.example.moabackend.domain.quiz.code.success;

import com.example.moabackend.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum QuizSuccessCode implements SuccessCode {
    SAVE_QUIZ_RESULT_SUCCESS(HttpStatus.OK, "퀴즈 결과를 성공적으로 저장하였습니다."),
    FIND_QUIZ_REMAIN_LIST_SUCCESS(HttpStatus.OK, "남은 퀴즈 결과를 성공적으로 불러왔습니다."),
    FIND_QUIZ_SET_SUCCESS(HttpStatus.OK, "요청한 퀴즈 세트(문제 목록)를 성공적으로 출제하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
