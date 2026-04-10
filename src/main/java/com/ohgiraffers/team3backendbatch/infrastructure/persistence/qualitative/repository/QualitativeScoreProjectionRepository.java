package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QualitativeScoreProjectionRepository extends JpaRepository<QualitativeScoreProjectionEntity, Long> {

    List<QualitativeScoreProjectionEntity> findAllByQualitativeEvaluationIdIn(Collection<Long> qualitativeEvaluationIds);

    List<QualitativeScoreProjectionEntity> findByEvaluationPeriodIdAndRawScoreIsNotNullAndNormalizedScoreIsNullOrderByEvaluationLevelAscQualitativeEvaluationIdAsc(
        Long evaluationPeriodId
    );

    List<QualitativeScoreProjectionEntity> findByEvaluationPeriodIdAndRawScoreIsNotNullOrderByEvaluationLevelAscQualitativeEvaluationIdAsc(
        Long evaluationPeriodId
    );

    @Query(
        value = """
            SELECT q.evaluation_period_id
            FROM batch_projection.qualitative_score_projection q
            WHERE q.raw_score IS NOT NULL
              AND q.normalized_score IS NULL
            GROUP BY q.evaluation_period_id
            ORDER BY MAX(COALESCE(q.analyzed_at, q.submitted_at)) DESC, q.evaluation_period_id DESC
            LIMIT 1
            """,
        nativeQuery = true
    )
    Long findLatestEvaluationPeriodIdForNormalization();

    @Query(
        value = """
            SELECT
                COUNT(q.raw_score) AS sampleCount,
                COALESCE(AVG(q.raw_score), 0) AS meanScore,
                COALESCE(STDDEV_SAMP(q.raw_score), 0) AS stddevScore
            FROM batch_projection.qualitative_score_projection q
            WHERE q.evaluation_period_id = :evaluationPeriodId
              AND q.raw_score IS NOT NULL
            """,
        nativeQuery = true
    )
    QualitativeScoreProjectionStatisticsView findNormalizationStatistics(@Param("evaluationPeriodId") Long evaluationPeriodId);

    @Query(
        value = """
            SELECT
                selected.evaluatee_id AS employeeId,
                selected.normalized_score AS normalizedScore
            FROM batch_projection.qualitative_score_projection selected
            JOIN (
                SELECT
                    q.evaluatee_id AS employeeId,
                    MAX(q.evaluation_level) AS latestEvaluationLevel
                FROM batch_projection.qualitative_score_projection q
                WHERE q.evaluation_period_id = :evaluationPeriodId
                  AND q.normalized_score IS NOT NULL
                GROUP BY q.evaluatee_id
            ) latest
              ON latest.employeeId = selected.evaluatee_id
             AND latest.latestEvaluationLevel = selected.evaluation_level
            WHERE selected.evaluation_period_id = :evaluationPeriodId
            ORDER BY selected.evaluatee_id
            """,
        nativeQuery = true
    )
    List<MonthlyQualitativeScoreView> findLatestNormalizedScoresByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    @Query(
        value = """
            SELECT
                selected.evaluatee_id AS employeeId,
                AVG(selected.normalized_score) AS normalizedScore
            FROM batch_projection.qualitative_score_projection selected
            JOIN (
                SELECT
                    q.evaluatee_id AS employeeId,
                    q.evaluation_period_id AS evaluationPeriodId,
                    MAX(q.evaluation_level) AS latestEvaluationLevel
                FROM batch_projection.qualitative_score_projection q
                JOIN batch_projection.evaluation_period_projection ep
                  ON ep.evaluation_period_id = q.evaluation_period_id
                WHERE ep.status = 'CONFIRMED'
                  AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN 28 AND 31
                  AND ep.start_date >= :startDate
                  AND ep.end_date <= :endDate
                  AND q.normalized_score IS NOT NULL
                GROUP BY q.evaluatee_id, q.evaluation_period_id
            ) latest
              ON latest.employeeId = selected.evaluatee_id
             AND latest.evaluationPeriodId = selected.evaluation_period_id
             AND latest.latestEvaluationLevel = selected.evaluation_level
            GROUP BY selected.evaluatee_id
            ORDER BY selected.evaluatee_id
            """,
        nativeQuery = true
    )
    List<MonthlyQualitativeScoreView> findAverageMonthlyNormalizedScoresByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query(
        value = """
            SELECT
                selected.evaluatee_id AS employeeId,
                selected.evaluation_period_id AS evaluationPeriodId,
                selected.normalized_score AS normalizedScore
            FROM batch_projection.qualitative_score_projection selected
            JOIN (
                SELECT
                    q.evaluatee_id AS employeeId,
                    q.evaluation_period_id AS evaluationPeriodId,
                    MAX(q.evaluation_level) AS latestEvaluationLevel
                FROM batch_projection.qualitative_score_projection q
                JOIN batch_projection.evaluation_period_projection ep
                  ON ep.evaluation_period_id = q.evaluation_period_id
                WHERE ep.status = 'CONFIRMED'
                  AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN 28 AND 31
                  AND ep.start_date >= :startDate
                  AND ep.end_date <= :endDate
                  AND q.normalized_score IS NOT NULL
                GROUP BY q.evaluatee_id, q.evaluation_period_id
            ) latest
              ON latest.employeeId = selected.evaluatee_id
             AND latest.evaluationPeriodId = selected.evaluation_period_id
             AND latest.latestEvaluationLevel = selected.evaluation_level
            ORDER BY selected.evaluatee_id, selected.evaluation_period_id
            """,
        nativeQuery = true
    )
    List<MonthlyQualitativeInspectionRow> findMonthlyNormalizedInspectionRowsByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    interface QualitativeScoreProjectionStatisticsView {
        Long getSampleCount();
        java.math.BigDecimal getMeanScore();
        java.math.BigDecimal getStddevScore();
    }

    interface MonthlyQualitativeScoreView {
        Long getEmployeeId();
        java.math.BigDecimal getNormalizedScore();
    }

    interface MonthlyQualitativeInspectionRow {
        Long getEmployeeId();
        Long getEvaluationPeriodId();
        java.math.BigDecimal getNormalizedScore();
    }
}
