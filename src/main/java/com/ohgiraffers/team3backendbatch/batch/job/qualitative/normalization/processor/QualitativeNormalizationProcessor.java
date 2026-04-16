package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationStatistics;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeEvaluatorStatisticsRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeScoreProjectionStatisticsRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeScoreQueryMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class QualitativeNormalizationProcessor
    implements ItemProcessor<QualitativeNormalizationTarget, QualitativeNormalizationResult> {

    private static final long MIN_SAMPLE_SIZE = 2L;
    private static final long MIN_EVALUATOR_SAMPLE_SIZE = 3L;
    private static final BigDecimal BIAS_EPSILON = new BigDecimal("0.01");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private final QualitativeScoreQueryMapper qualitativeScoreQueryMapper;
    private final QualitativeScoreCalculator qualitativeScoreCalculator;
    private final Long requestedEvaluationPeriodId;

    private QualitativeNormalizationStatistics statistics;
    private Map<Long, EvaluatorBiasStats> evaluatorBiasStatsByEvaluatorId;
    private Long resolvedEvaluationPeriodId;
    private boolean insufficientSampleLogged;

    /**
     * 정성 점수 정규화 Processor 를 생성한다.
     * @param qualitativeScoreQueryMapper 정성 점수 조회 mapper
     * @param qualitativeScoreCalculator 정성 점수 계산기
     * @param evaluationPeriodId 평가 기간 ID
     */
    public QualitativeNormalizationProcessor(
        QualitativeScoreQueryMapper qualitativeScoreQueryMapper,
        QualitativeScoreCalculator qualitativeScoreCalculator,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId
    ) {
        this.qualitativeScoreQueryMapper = qualitativeScoreQueryMapper;
        this.qualitativeScoreCalculator = qualitativeScoreCalculator;
        this.requestedEvaluationPeriodId = evaluationPeriodId;
    }

    /**
     * 정성 원점수를 정규화 점수와 등급으로 변환한다.
     * @param item 정성 점수 정규화 대상 데이터
     * @return 정성 점수 정규화 결과
     */
    @Override
    public QualitativeNormalizationResult process(QualitativeNormalizationTarget item) {
        QualitativeNormalizationStatistics normalizationStatistics = getStatistics();
        if (normalizationStatistics.getSampleCount() < MIN_SAMPLE_SIZE) {
            if (!insufficientSampleLogged) {
                insufficientSampleLogged = true;
                log.warn(
                    "Skipping qualitative normalization due to insufficient samples. requestedEvaluationPeriodId={}, resolvedEvaluationPeriodId={}, sampleCount={}",
                    requestedEvaluationPeriodId,
                    resolvedEvaluationPeriodId,
                    normalizationStatistics.getSampleCount()
                );
            }
            return null;
        }

        EvaluatorBiasMetadata biasMetadata = resolveBiasMetadata(item, normalizationStatistics);
        BigDecimal effectiveRawScore = biasMetadata.correctedRawScore();
        var sQual = qualitativeScoreCalculator.normalizeToTScore(
            effectiveRawScore,
            normalizationStatistics.getMeanScore(),
            normalizationStatistics.getStddevScore()
        );
        var grade = qualitativeScoreCalculator.classifyTier(sQual);

        return QualitativeNormalizationResult.builder()
            .evaluationId(item.getEvaluationId())
            .originalRawScore(item.getRawScore())
            .rawScore(effectiveRawScore)
            .sQual(sQual)
            .grade(grade)
            .biasCorrected(biasMetadata.biasCorrected())
            .biasType(biasMetadata.biasType())
            .evaluatorAverage(biasMetadata.evaluatorAverage())
            .companyAverage(biasMetadata.companyAverage())
            .alphaBias(biasMetadata.alphaBias())
            .build();
    }

    /**
     * 정규화에 사용할 통계 값을 조회한다.
     * @param 없음
     * @return 정성 점수 정규화 통계 값
     */
    private QualitativeNormalizationStatistics getStatistics() {
        if (statistics == null) {
            resolvedEvaluationPeriodId = requestedEvaluationPeriodId != null
                ? requestedEvaluationPeriodId
                : qualitativeScoreQueryMapper.findLatestEvaluationPeriodIdForNormalization();

            if (resolvedEvaluationPeriodId == null) {
                statistics = new QualitativeNormalizationStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO);
                return statistics;
            }

            QualitativeScoreProjectionStatisticsRow view =
                qualitativeScoreQueryMapper.findNormalizationStatistics(resolvedEvaluationPeriodId);

            if (view == null) {
                statistics = new QualitativeNormalizationStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO);
            } else {
                statistics = new QualitativeNormalizationStatistics(
                    view.getSampleCount() == null ? 0L : view.getSampleCount(),
                    view.getMeanScore() == null ? BigDecimal.ZERO : view.getMeanScore(),
                    view.getStddevScore() == null ? BigDecimal.ZERO : view.getStddevScore()
                );
            }
        }
        return statistics;
    }

    private Map<Long, EvaluatorBiasStats> getEvaluatorBiasStats() {
        if (evaluatorBiasStatsByEvaluatorId == null) {
            evaluatorBiasStatsByEvaluatorId = new LinkedHashMap<>();
            if (resolvedEvaluationPeriodId == null) {
                getStatistics();
            }
            if (resolvedEvaluationPeriodId == null) {
                return evaluatorBiasStatsByEvaluatorId;
            }

            for (QualitativeEvaluatorStatisticsRow row
                : qualitativeScoreQueryMapper.findEvaluatorNormalizationStatistics(resolvedEvaluationPeriodId)) {
                if (row.getEvaluatorId() == null) {
                    continue;
                }
                evaluatorBiasStatsByEvaluatorId.put(
                    row.getEvaluatorId(),
                    new EvaluatorBiasStats(
                        row.getSampleCount() == null ? 0L : row.getSampleCount(),
                        row.getMeanScore() == null ? ZERO : row.getMeanScore().setScale(2, RoundingMode.HALF_UP)
                    )
                );
            }
        }
        return evaluatorBiasStatsByEvaluatorId;
    }

    private EvaluatorBiasMetadata resolveBiasMetadata(
        QualitativeNormalizationTarget item,
        QualitativeNormalizationStatistics normalizationStatistics
    ) {
        BigDecimal originalRawScore = item.getRawScore() == null ? ZERO : item.getRawScore().setScale(2, RoundingMode.HALF_UP);
        BigDecimal companyAverage = normalizationStatistics.getMeanScore() == null
            ? ZERO
            : normalizationStatistics.getMeanScore().setScale(2, RoundingMode.HALF_UP);

        if (item.getEvaluatorId() == null) {
            return new EvaluatorBiasMetadata(originalRawScore, false, null, null, companyAverage, null);
        }

        EvaluatorBiasStats evaluatorBiasStats = getEvaluatorBiasStats().get(item.getEvaluatorId());
        if (evaluatorBiasStats == null || evaluatorBiasStats.sampleCount() < MIN_EVALUATOR_SAMPLE_SIZE) {
            return new EvaluatorBiasMetadata(originalRawScore, false, null, null, companyAverage, null);
        }

        BigDecimal alphaBias = evaluatorBiasStats.meanScore().subtract(companyAverage).setScale(2, RoundingMode.HALF_UP);
        if (alphaBias.abs().compareTo(BIAS_EPSILON) < 0) {
            return new EvaluatorBiasMetadata(originalRawScore, false, null, evaluatorBiasStats.meanScore(), companyAverage, alphaBias);
        }

        BigDecimal correctedRawScore = clampDisplayScore(originalRawScore.subtract(alphaBias));
        String biasType = alphaBias.compareTo(BigDecimal.ZERO) > 0 ? "LENIENT" : "STRICT";
        return new EvaluatorBiasMetadata(
            correctedRawScore,
            true,
            biasType,
            evaluatorBiasStats.meanScore(),
            companyAverage,
            alphaBias
        );
    }

    private BigDecimal clampDisplayScore(BigDecimal score) {
        if (score == null) {
            return ZERO;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        if (score.compareTo(HUNDRED) > 0) {
            return HUNDRED;
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private record EvaluatorBiasStats(long sampleCount, BigDecimal meanScore) {
    }

    private record EvaluatorBiasMetadata(
        BigDecimal correctedRawScore,
        boolean biasCorrected,
        String biasType,
        BigDecimal evaluatorAverage,
        BigDecimal companyAverage,
        BigDecimal alphaBias
    ) {
    }
}
