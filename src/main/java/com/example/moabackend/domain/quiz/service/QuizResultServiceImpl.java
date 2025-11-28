package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.req.QuizSubmitRequestDto;
import com.example.moabackend.domain.quiz.dto.res.result.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.dto.res.result.QuizSubmitResponseDto;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizQuestionRepository;
import com.example.moabackend.domain.quiz.repository.QuizResultRepository;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.report.event.ReportEventProducer;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizResultServiceImpl implements QuizResultService {
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final QuizConverter quizConverter;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ReportEventProducer reportEventProducer;

    @Override
    @Transactional
    public QuizSubmitResponseDto submitAndScoreAnswer(Long userId, QuizSubmitRequestDto requestDto) {
        QuizQuestion question = quizQuestionRepository.findById(requestDto.questionId())
                .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        boolean isCorrect = question.getAnswer().trim().equalsIgnoreCase(requestDto.userAnswer().trim());
        int correctCount = isCorrect ? 1 : 0;

        QuizSaveRequestDto saveRequest = new QuizSaveRequestDto(1, correctCount, question.getType());

        saveQuizResult(userId, saveRequest);

        return new QuizSubmitResponseDto(
                question.getId(),
                question.getType(),
                isCorrect,
                question.getAnswer());
    }

    @Override
    @Transactional
    public void saveQuizResult(Long userId, QuizSaveRequestDto quizSaveRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Optional<QuizResult> quiz = quizResultRepository.findByUserIdAndDateAndTypeLocked(userId, LocalDate.now(),
                quizSaveRequestDto.type());

        if (quiz.isPresent()) {
            quiz.get().updateCorrectNumber(quizSaveRequestDto.correctNumber());
            quiz.get().updateTotalNumber(quizSaveRequestDto.totalNumber());
        } else {
            try {
                quizResultRepository.save(quizConverter.toEntity(user, quizSaveRequestDto));
            } catch (DataIntegrityViolationException e) {
                throw new CustomException(QuizErrorCode.ALREADY_SUBMITTED);
            }
        }

        updateAllTypeResult(user, LocalDate.now());

        // 퀴즈 저장 후 모든 퀴즈 완료 여부 확인
        if (hasCompletedAllQuiz(userId, LocalDate.now())) {
            reportEventProducer.publishReportEvent(userId, EReportType.DAILY.getValue());
        }
    }

    @Transactional
    public void updateAllTypeResult(User user, LocalDate date) {
        List<QuizResult> results = quizResultRepository
                .findAllByUserIdAndDate(user.getId(), date);

        Optional<QuizResult> allOpt = results.stream()
                .filter(r -> r.getType() == EQuizType.ALL)
                .findFirst();

        int totalCorrect = results.stream()
                .filter(r -> r.getType() != EQuizType.ALL)
                .mapToInt(QuizResult::getCorrectNumber)
                .sum();

        int totalTotal = results.stream()
                .filter(r -> r.getType() != EQuizType.ALL)
                .mapToInt(QuizResult::getTotalNumber)
                .sum();

        QuizResult allResult;

        if (allOpt.isPresent()) {
            allResult = allOpt.get();
            allResult.updateCorrectNumber(totalCorrect);
            allResult.updateTotalNumber(totalTotal);
        } else {
            quizResultRepository.save(
                    QuizResult.builder()
                            .user(user)
                            .type(EQuizType.ALL)
                            .correctNumber(totalCorrect)
                            .totalNumber(totalTotal)
                            .date(date)
                            .build()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasCompletedAllQuiz(Long userId, LocalDate date) {
        int completedCount = quizResultRepository.getCompletedQuizTypes(userId, date).size();
        return completedCount == EQuizType.values().length;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizRemainTypeResponseDto remainTypeQuiz(Long userId, LocalDate date) {
        List<EQuizType> completed = quizResultRepository.getCompletedQuizTypes(userId, date);
        List<EQuizType> remainList = Arrays.stream(EQuizType.values())
                .filter(type -> !completed.contains(type))
                .toList();
        return QuizRemainTypeResponseDto.of(remainList);
    }
}