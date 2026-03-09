package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    @Query(value = "SELECT qq.quiz_question_id FROM quiz_question qq WHERE qq.quiz_type = :type ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Long> findQuizIdsByType(@Param("type") String type, @Param("count") int count);

    @Query(value = """
            
            (SELECT qq.quiz_question_id FROM quiz_question qq WHERE qq.quiz_type = 'PERSISTENCE' ORDER BY RAND() LIMIT 3)
                       UNION ALL (SELECT qq.quiz_question_id FROM quiz_question qq WHERE qq.quiz_type = 'MEMORY' ORDER BY RAND() LIMIT 3)
                             UNION ALL (SELECT qq.quiz_question_id FROM quiz_question qq WHERE qq.quiz_type = 'LINGUISTIC' ORDER BY RAND() LIMIT 3)
                             UNION ALL (SELECT qq.quiz_question_id FROM quiz_question qq WHERE qq.quiz_type = 'ATTENTION' ORDER BY RAND() LIMIT 3)
                             UNION ALL (SELECT qq.quiz_question_id FROM quiz_question qq WHERE qq.quiz_type = 'SPACETIME' ORDER BY RAND() LIMIT 3)
            """, nativeQuery = true)
    List<Long> findTodayQuizIds();
}