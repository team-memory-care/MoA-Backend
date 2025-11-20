package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    @Query(value = "SELECT qq.id FROM quiz_question qq WHERE qq.type = :type ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Long> findQuizIdsByType(@Param("type") String type, @Param("count") int count);

    @Query(value = """
            
            (SELECT qq.id FROM quiz_question qq WHERE qq.type = 'PERSISTENCE' ORDER BY RAND() LIMIT 3)
                       UNION ALL (SELECT qq.id FROM quiz_question qq WHERE qq.type = 'MEMORY' ORDER BY RAND() LIMIT 3)
                             UNION ALL (SELECT qq.id FROM quiz_question qq WHERE qq.type = 'LINGUISTIC' ORDER BY RAND() LIMIT 3)
                             UNION ALL (SELECT qq.id FROM quiz_question qq WHERE qq.type = 'ATTENTION' ORDER BY RAND() LIMIT 3)
                             UNION ALL (SELECT qq.id FROM quiz_question qq WHERE qq.type = 'SPACETIME' ORDER BY RAND() LIMIT 3)
            """, nativeQuery = true)
    List<Long> findTodayQuizIds();
}