package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.req.QuizSubmitRequestDto;
import com.example.moabackend.domain.quiz.dto.res.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.dto.res.QuizSubmitResponseDto;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizQuestionRepository;
import com.example.moabackend.domain.quiz.repository.QuizResultRepository;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizResultServiceImpl implements QuizResultService {
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final QuizConverter quizConverter;
    private final QuizQuestionRepository quizQuestionRepository;


    @Override
    @Transactional
    public QuizSubmitResponseDto submitAndScoreAnswer(Long userId, QuizSubmitRequestDto requestDto) {
        QuizQuestion question = quizQuestionRepository.findById(requestDto.questionId())
                .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        boolean isCorrect = question.getAnswer().trim().equalsIgnoreCase(requestDto.userAnswer().trim());
        QuizResult quizResult = QuizResult.builder()
                .user(user)
                .question(question)
                .totalNumber(1)
                .correctNumber(isCorrect ? 1 : 0)
                .date(LocalDate.now())
                .type(question.getType())
                .build();
        quizResultRepository.save(quizResult);

        return new QuizSubmitResponseDto(
                question.getId(),
                question.getType(),
                isCorrect,
                question.getAnswer()
        );
    }


    @Override
    @Transactional
    public void saveQuizResult(Long userId, QuizSaveRequestDto quizSaveRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Optional<QuizResult> quiz = quizResultRepository.findByUserIdAndDateAndType(userId, LocalDate.now(), quizSaveRequestDto.type());

        if (quiz.isPresent()) {
            quiz.get().updateCorrectNumber(quizSaveRequestDto.correctNumber());
        } else {
            quizResultRepository.save(quizConverter.toEntity(user, quizSaveRequestDto));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasCompletedAllQuiz(Long userId, LocalDate date) {
        int completedCount = quizResultRepository.findCompletedQuizTypes(userId, date).size();
        return completedCount == EQuizType.values().length;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizRemainTypeResponseDto remainTypeQuiz(Long userId, LocalDate date) {
        List<EQuizType> completed = quizResultRepository.findCompletedQuizTypes(userId, date);
        List<EQuizType> remainList = Arrays.stream(EQuizType.values())
                .filter(type -> !completed.contains(type))
                .toList();
        return QuizRemainTypeResponseDto.of(remainList);
    }
}