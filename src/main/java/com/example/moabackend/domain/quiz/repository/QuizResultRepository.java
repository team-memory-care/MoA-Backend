package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    // 일반 조회
    Optional<QuizResult> findByUserIdAndDateAndType(Long userId, LocalDate date, EQuizType quizType);

    // 락킹 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT qr FROM QuizResult qr WHERE qr.user.id = :userId AND qr.date = :date AND qr.type = :quizType")
    Optional<QuizResult> findByUserIdAndDateAndTypeLocked(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("quizType") EQuizType quizType
    );

    // 왼료 퀴즈 조회
    @Query("""
                SELECT DISTINCT q.type
                FROM QuizResult q
                WHERE q.user.id = :userId
                AND q.date = :date 
            """)

    List<EQuizType> getCompletedQuizTypes(@Param("userId") Long userId, @Param("date") LocalDate date);
}