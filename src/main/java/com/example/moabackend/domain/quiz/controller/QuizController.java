package com.example.moabackend.domain.quiz.controller;

import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.res.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.code.success.QuizSuccessCode;
import com.example.moabackend.domain.quiz.service.QuizService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quiz")
@Tag(name = "Quiz", description = "퀴즈 API")
public class QuizController {
    private final QuizService quizService;

    @Operation(summary = "퀴즈 결과 저장",
            description = "퀴즈 결과를 저장하는 API입니다.<br>이미 푼 퀴즈라면 맞은 개수만 업데이트, 처음 푼 퀴즈라면 새로 저장을 진행합니다.")
    @PostMapping("/result")
    public BaseResponse<Void> saveQuizResult(
            @UserId Long userId,
            @RequestBody QuizSaveRequestDto quizSaveRequestDto
    ) {
        quizService.saveQuizResult(userId, quizSaveRequestDto);
        return BaseResponse.success(QuizSuccessCode.SAVE_QUIZ_RESULT_SUCCESS, null);
    }

    @Operation(summary = "남은 퀴즈 확인",
            description = "남은 퀴즈 종류를 확인합니다.<br>PERSISTENCE, MEMORY, LINGUISTIC, ATTENTION, SPACETIME이 나올 수 있습니다.")
    @GetMapping("/remain")
    public BaseResponse<QuizRemainTypeResponseDto> getQuizRemainType(
            @UserId Long userId,
            @RequestParam @Parameter(description = "yyyy-mm-dd 같은 형식으로 작성해야합니다.") LocalDate date
    ) {
        return BaseResponse.success(QuizSuccessCode.FIND_QUIZ_REMAIN_LIST_SUCCESS, quizService.remainTypeQuiz(userId, date));
    }
}
