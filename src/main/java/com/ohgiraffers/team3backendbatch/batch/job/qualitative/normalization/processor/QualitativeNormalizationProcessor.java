package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationStatistics;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeEvaluationQueryMapper;
import java.math.BigDecimal;
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

    private final QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper;
    private final QualitativeScoreCalculator qualitativeScoreCalculator;
    private final Long requestedEvaluationPeriodId;

    private QualitativeNormalizationStatistics statistics;
    private Long resolvedEvaluationPeriodId;
    private boolean insufficientSampleLogged;

    public QualitativeNormalizationProcessor(
        QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper,
        QualitativeScoreCalculator qualitativeScoreCalculator,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId
    ) {
        this.qualitativeEvaluationQueryMapper = qualitativeEvaluationQueryMapper;
        this.qualitativeScoreCalculator = qualitativeScoreCalculator;
        this.requestedEvaluationPeriodId = evaluationPeriodId;
    }

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

        var sQual = qualitativeScoreCalculator.normalizeToTScore(
            item.getRawScore(),
            normalizationStatistics.getMeanScore(),
            normalizationStatistics.getStddevScore()
        );
        var grade = qualitativeScoreCalculator.classifyTier(sQual);

        return QualitativeNormalizationResult.builder()
            .evaluationId(item.getEvaluationId())
            .rawScore(item.getRawScore())
            .sQual(sQual)
            .grade(grade)
            .build();
    }

    private QualitativeNormalizationStatistics getStatistics() {
        if (statistics == null) {
            resolvedEvaluationPeriodId = requestedEvaluationPeriodId != null
                ? requestedEvaluationPeriodId
                : qualitativeEvaluationQueryMapper.findLatestEvaluationPeriodIdForNormalization();

            if (resolvedEvaluationPeriodId == null) {
                statistics = new QualitativeNormalizationStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO);
                return statistics;
            }

            statistics = qualitativeEvaluationQueryMapper.findQualitativeNormalizationStatistics(resolvedEvaluationPeriodId);
            if (statistics == null) {
                statistics = new QualitativeNormalizationStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        }
        return statistics;
    }
}