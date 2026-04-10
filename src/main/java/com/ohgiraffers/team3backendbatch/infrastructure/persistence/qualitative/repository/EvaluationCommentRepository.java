package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.EvaluationCommentEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationCommentRepository extends JpaRepository<EvaluationCommentEntity, Long> {

    @Query(value = """
        SELECT
            q.evaluatee_id AS employeeId,
            ec.matched_keywords AS matchedKeywords,
            ec.matched_keyword_details AS matchedKeywordDetails
        FROM evaluation_comment ec
        JOIN qualitative_evaluation q
          ON q.qualitative_evaluation_id = ec.qualitative_evaluation_id
        WHERE q.evaluation_period_id = :evaluationPeriodId
        """, nativeQuery = true)
    List<MonthlyMatchedKeywordView> findMatchedKeywordsByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    @Query(value = """
        SELECT
            q.evaluatee_id AS employeeId,
            ec.matched_keywords AS matchedKeywords,
            ec.matched_keyword_details AS matchedKeywordDetails
        FROM evaluation_comment ec
        JOIN qualitative_evaluation q
          ON q.qualitative_evaluation_id = ec.qualitative_evaluation_id
        JOIN batch_projection.evaluation_period_projection ep
          ON ep.evaluation_period_id = q.evaluation_period_id
        WHERE ep.status = 'CONFIRMED'
          AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN 28 AND 31
          AND ep.start_date >= :startDate
          AND ep.end_date <= :endDate
        """, nativeQuery = true)
    List<MonthlyMatchedKeywordView> findMatchedKeywordsByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    interface MonthlyMatchedKeywordView {
        Long getEmployeeId();
        String getMatchedKeywords();
        String getMatchedKeywordDetails();
    }
}
