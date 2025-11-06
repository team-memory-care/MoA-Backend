package com.example.moabackend.domain.quiz.controller;

import com.example.moabackend.domain.quiz.code.success.QuizSuccessCode;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.res.QuizQuestionDto;
import com.example.moabackend.domain.quiz.dto.res.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.service.QuizQuestionService;
import com.example.moabackend.domain.quiz.service.QuizResultService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quiz")
@Tag(name = "Quiz", description = "퀴즈 API")
public class QuizController {
    private final QuizQuestionService quizQuestionService;
    private final QuizResultService quizResultService;

    @Operation(summary = "오늘의 퀴즈 세트 출제",
            description = "5가지 유형별로 3개씩, 총 15개의 문제를 랜점으로 출제합니다.")
    @GetMapping("/today")
    public BaseResponse<List<QuizQuestionDto>> getTodayQuizSet(
            @UserId Long userId
    ) {
        List<QuizQuestionDto> questions = quizQuestionService.getTodayQuizSet(userId);
        return BaseResponse.success(QuizSuccessCode.FIND_QUIZ_SET_SUCCESS, questions);
    }

    @Operation(summary = "단일 유형 퀴즈 세트 출제",
            description = "특정 퀴즈 유형에 해당하는 문제를 5개씩 출제합니다.")
    @GetMapping("/set")
    public BaseResponse<List<QuizQuestionDto>> getQuizSetByType(
            @UserId Long userId,
            @RequestParam @Parameter(description = "출제할 퀴즈 유형") EQuizType type
    ) {
        List<QuizQuestionDto> questions = quizQuestionService.getQuizSetByType(userId, type);
        return BaseResponse.success(QuizSuccessCode.FIND_QUIZ_SET_SUCCESS, questions);
    }

    @Operation(summary = "퀴즈 결과 저장",
            description = "퀴즈 결과를 저장하는 API입니다.<br>이미 푼 퀴즈라면 맞은 개수만 업데이트, 처음 푼 퀴즈라면 새로 저장을 진행합니다.")
    @PostMapping("/result")
    public BaseResponse<Void> saveQuizResult(
            @UserId Long userId,
            @RequestBody QuizSaveRequestDto quizSaveRequestDto
    ) {
        quizResultService.saveQuizResult(userId, quizSaveRequestDto);
        return BaseResponse.success(QuizSuccessCode.SAVE_QUIZ_RESULT_SUCCESS, null);
    }

    @Operation(summary = "남은 퀴즈 확인",
            description = "남은 퀴즈 종류를 확인합니다.<br>PERSISTENCE, MEMORY, LINGUISTIC, ATTENTION, SPACETIME이 나올 수 있습니다.")
    @GetMapping("/remain")
    public BaseResponse<QuizRemainTypeResponseDto> getQuizRemainType(
            @UserId Long userId,
            @RequestParam @Parameter(description = "yyyy-mm-dd 같은 형식으로 작성해야합니다.") LocalDate date
    ) {
        return BaseResponse.success(QuizSuccessCode.FIND_QUIZ_REMAIN_LIST_SUCCESS, quizResultService.remainTypeQuiz(userId, date));
    }
}
