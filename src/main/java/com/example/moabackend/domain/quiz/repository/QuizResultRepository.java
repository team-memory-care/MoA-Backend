package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<QuizResult> findByUserIdAndDateAndType(Long userId, LocalDate date, EQuizType quizType);

    @Query("""
                SELECT DISTINCT q.type
                FROM QuizResult q
                WHERE q.user.id = :userId
                AND DATE(q.date) = :date
            """)
    List<EQuizType> findCompletedQuizTypes(@Param("userId") Long userId, @Param("date") LocalDate date);
}