package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
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
            FROM qualitative_score_projection q
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
            FROM qualitative_score_projection q
            WHERE q.evaluation_period_id = :evaluationPeriodId
              AND q.raw_score IS NOT NULL
            """,
        nativeQuery = true
    )
    QualitativeScoreProjectionStatisticsView findNormalizationStatistics(@Param("evaluationPeriodId") Long evaluationPeriodId);

    interface QualitativeScoreProjectionStatisticsView {
        Long getSampleCount();
        java.math.BigDecimal getMeanScore();
        java.math.BigDecimal getStddevScore();
    }
}