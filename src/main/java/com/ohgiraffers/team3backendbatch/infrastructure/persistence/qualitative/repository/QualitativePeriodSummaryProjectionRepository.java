package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativePeriodSummaryProjectionEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QualitativePeriodSummaryProjectionRepository
    extends JpaRepository<QualitativePeriodSummaryProjectionEntity, Long> {

    Optional<QualitativePeriodSummaryProjectionEntity> findByEvaluationPeriodIdAndEvaluateeId(
        Long evaluationPeriodId,
        Long evaluateeId
    );

    @Query(
        value = """
            SELECT
                :evaluationPeriodId AS evaluationPeriodId,
                selected.evaluatee_id AS evaluateeId,
                COUNT(*) AS sourceMonthCount,
                COALESCE(AVG(selected.raw_score), 0) AS averageRawScore,
                COALESCE(AVG(selected.normalized_score), 0) AS averageNormalizedScore
            FROM qualitative_score_projection selected
            JOIN (
                SELECT
                    qsp.evaluation_period_id AS evaluationPeriodId,
                    qsp.evaluatee_id AS evaluateeId,
                    MAX(qsp.evaluation_level) AS latestEvaluationLevel
                FROM qualitative_score_projection qsp
                JOIN evaluation_period source_ep
                  ON source_ep.eval_period_id = qsp.evaluation_period_id
                JOIN evaluation_period target_ep
                  ON target_ep.eval_period_id = :evaluationPeriodId
                WHERE qsp.normalized_score IS NOT NULL
                  AND qsp.evaluation_period_id <> :evaluationPeriodId
                  AND source_ep.start_date >= target_ep.start_date
                  AND source_ep.end_date <= target_ep.end_date
                  AND TIMESTAMPDIFF(MONTH, source_ep.start_date, source_ep.end_date) = 0
                  AND (:employeeId IS NULL OR qsp.evaluatee_id = :employeeId)
                GROUP BY qsp.evaluation_period_id, qsp.evaluatee_id
            ) latest
              ON latest.evaluationPeriodId = selected.evaluation_period_id
             AND latest.evaluateeId = selected.evaluatee_id
             AND latest.latestEvaluationLevel = selected.evaluation_level
            GROUP BY selected.evaluatee_id
            ORDER BY selected.evaluatee_id
            """,
        nativeQuery = true
    )
    List<QualitativePeriodSummaryCandidateView> findSummaryCandidates(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("employeeId") Long employeeId
    );

    interface QualitativePeriodSummaryCandidateView {
        Long getEvaluationPeriodId();
        Long getEvaluateeId();
        Long getSourceMonthCount();
        BigDecimal getAverageRawScore();
        BigDecimal getAverageNormalizedScore();
    }
}
