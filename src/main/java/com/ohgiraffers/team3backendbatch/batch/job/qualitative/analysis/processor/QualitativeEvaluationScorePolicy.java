package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeCommentAnalysis;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationScoreResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeScoreCalculator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies evaluation-level policy to a comment analysis result.
 * Level 1 creates the base raw score.
 * Level 2 either keeps the first raw score or adds a raw-score adjustment.
 * Normalized score and grade are finalized later in monthly normalization.
 */
@Component
@RequiredArgsConstructor
public class QualitativeEvaluationScorePolicy {

    private final QualitativeScoreCalculator qualitativeScoreCalculator;

    public QualitativeEvaluationScoreResult apply(
        QualitativeEvaluationAggregate aggregate,
        QualitativeCommentAnalysis commentAnalysis
    ) {
        Long evaluationLevel = aggregate.getEvaluationLevel();

        if (evaluationLevel == null || evaluationLevel == 1L) {
            return buildFirstEvaluationResult(requireCommentAnalysis(commentAnalysis));
        }

        if (evaluationLevel == 2L) {
            return buildSecondEvaluationResult(aggregate, commentAnalysis);
        }

        throw new IllegalStateException("Final evaluation level is not processed by qualitative analysis batch.");
    }

    private QualitativeEvaluationScoreResult buildFirstEvaluationResult(QualitativeCommentAnalysis commentAnalysis) {
        return QualitativeEvaluationScoreResult.builder()
            .finalRawScore(commentAnalysis.getOfficialRawScore())
            .originalSQual(null)
            .finalSQual(null)
            .adjustmentScore(BigDecimal.ZERO.setScale(4))
            .normalizedTier(null)
            .build();
    }

    private QualitativeEvaluationScoreResult buildSecondEvaluationResult(
        QualitativeEvaluationAggregate aggregate,
        QualitativeCommentAnalysis commentAnalysis
    ) {
        BigDecimal baseRawScore = requireBaseRawScore(aggregate);
        SecondEvaluationMode secondEvaluationMode = aggregate.getSecondEvaluationMode();

        if (secondEvaluationMode == null) {
            throw new IllegalStateException("Second evaluation mode is required for evaluation level 2.");
        }

        if (secondEvaluationMode == SecondEvaluationMode.KEEP_FIRST_SCORE) {
            return QualitativeEvaluationScoreResult.builder()
                .finalRawScore(baseRawScore)
                .originalSQual(null)
                .finalSQual(null)
                .adjustmentScore(BigDecimal.ZERO.setScale(4))
                .normalizedTier(null)
                .build();
        }

        QualitativeCommentAnalysis requiredCommentAnalysis = requireCommentAnalysis(commentAnalysis);
        BigDecimal officialCommentRaw = requiredCommentAnalysis.getOfficialRawScore();
        BigDecimal adjustmentScore = qualitativeScoreCalculator.calculateSecondaryAdjustmentRaw(officialCommentRaw);
        BigDecimal finalRawScore = qualitativeScoreCalculator.applyRawAdjustment(baseRawScore, adjustmentScore);

        return QualitativeEvaluationScoreResult.builder()
            .finalRawScore(finalRawScore)
            .originalSQual(null)
            .finalSQual(null)
            .adjustmentScore(adjustmentScore)
            .normalizedTier(null)
            .build();
    }

    private BigDecimal requireBaseRawScore(QualitativeEvaluationAggregate aggregate) {
        BigDecimal baseRawScore = aggregate.getBaseRawScore();
        if (baseRawScore == null) {
            throw new IllegalStateException("Base first evaluation raw score is missing for secondary evaluation.");
        }
        return baseRawScore;
    }

    private QualitativeCommentAnalysis requireCommentAnalysis(QualitativeCommentAnalysis commentAnalysis) {
        if (commentAnalysis == null) {
            throw new IllegalStateException("Comment analysis is required for comment-based qualitative evaluation.");
        }
        return commentAnalysis;
    }
}