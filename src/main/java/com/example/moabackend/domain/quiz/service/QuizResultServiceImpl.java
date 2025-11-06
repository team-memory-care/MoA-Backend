package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.res.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
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
    private final QuizResultRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizConverter quizConverter;

    @Override
    @Transactional
    public void saveQuizResult(Long userId, QuizSaveRequestDto quizSaveRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Optional<QuizResult> quiz = quizRepository.findByUserIdAndDateAndType(userId, LocalDate.now(), quizSaveRequestDto.type());

        if (quiz.isPresent()) {
            quiz.get().updateCorrectNumber(quizSaveRequestDto.correctNumber());
        } else {
            quizRepository.save(quizConverter.toEntity(user, quizSaveRequestDto));
        }
    }

    @Override
    @Transactional
    public Boolean hasCompletedAllQuiz(Long userId, LocalDate date) {
        int completedCount = quizRepository.findCompletedQuizTypes(userId, date).size();
        return completedCount == EQuizType.values().length;
    }

    @Override
    @Transactional
    public QuizRemainTypeResponseDto remainTypeQuiz(Long userId, LocalDate date) {
        List<EQuizType> completed = quizRepository.findCompletedQuizTypes(userId, date);
        List<EQuizType> remainList = Arrays.stream(EQuizType.values())
                .filter(type -> !completed.contains(type))
                .toList();
        return QuizRemainTypeResponseDto.of(remainList);
    }
}
