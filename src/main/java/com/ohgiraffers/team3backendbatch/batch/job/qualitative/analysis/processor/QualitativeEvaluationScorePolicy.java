package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeCommentAnalysis;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationScoreResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies evaluation-level policy to a comment analysis result.
 * Level 1 creates the base raw score.
 * Level 2 either keeps the first raw score or adds a raw-score adjustment.
 */
@Component
@RequiredArgsConstructor
public class QualitativeEvaluationScorePolicy {

    private final QualitativeScoreCalculator qualitativeScoreCalculator;

    /**
     * 평가 단계별 점수 정책을 적용한다.
     * @param aggregate 정성 평가 집계 원본 데이터
     * @param commentAnalysis 코멘트 분석 결과
     * @return 단계별 정책이 반영된 정성 평가 점수 결과
     */
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

    /**
     * 1차 평가 결과를 생성한다.
     * @param commentAnalysis 1차 코멘트 분석 결과
     * @return 1차 평가 점수 결과
     */
    private QualitativeEvaluationScoreResult buildFirstEvaluationResult(QualitativeCommentAnalysis commentAnalysis) {
        return buildScoreResult(
            qualitativeScoreCalculator.scaleInternalRawToDisplayScore(commentAnalysis.getOfficialRawScore()),
            BigDecimal.ZERO.setScale(2)
        );
    }

    /**
     * 2차 평가 결과를 생성한다.
     * @param aggregate 정성 평가 집계 원본 데이터
     * @param commentAnalysis 2차 코멘트 분석 결과
     * @return 2차 평가 점수 결과
     */
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
            return buildScoreResult(baseRawScore, BigDecimal.ZERO.setScale(2));
        }

        QualitativeCommentAnalysis requiredCommentAnalysis = requireCommentAnalysis(commentAnalysis);
        BigDecimal officialCommentRaw = requiredCommentAnalysis.getOfficialRawScore();
        BigDecimal internalAdjustmentScore = qualitativeScoreCalculator.calculateSecondaryAdjustmentRaw(officialCommentRaw);
        BigDecimal adjustmentScore = qualitativeScoreCalculator.scaleInternalAdjustmentToDisplayDelta(internalAdjustmentScore);
        BigDecimal finalRawScore = qualitativeScoreCalculator.applyDisplayRawAdjustment(baseRawScore, adjustmentScore);

        return buildScoreResult(finalRawScore, adjustmentScore);
    }

    /**
     * 최종 점수 결과 객체를 생성한다.
     * @param finalRawScore 최종 표시 점수
     * @param adjustmentScore 2차 평가 보정 점수
     * @return 저장 및 후속 처리에 사용할 점수 결과 객체
     */
    private QualitativeEvaluationScoreResult buildScoreResult(
        BigDecimal finalRawScore,
        BigDecimal adjustmentScore
    ) {
        String normalizedTier = qualitativeScoreCalculator.classifyTier(finalRawScore);

        return QualitativeEvaluationScoreResult.builder()
            .finalRawScore(finalRawScore)
            .originalSQual(finalRawScore)
            .finalSQual(finalRawScore)
            .adjustmentScore(adjustmentScore)
            .normalizedTier(normalizedTier)
            .build();
    }

    /**
     * 2차 평가의 기준 원점수를 확인한다.
     * @param aggregate 정성 평가 집계 원본 데이터
     * @return 1차 평가에서 계산된 기준 원점수
     */
    private BigDecimal requireBaseRawScore(QualitativeEvaluationAggregate aggregate) {
        BigDecimal baseRawScore = aggregate.getBaseRawScore();
        if (baseRawScore == null) {
            throw new IllegalStateException("Base first evaluation raw score is missing for secondary evaluation.");
        }
        return baseRawScore;
    }

    /**
     * 코멘트 분석 결과 존재 여부를 확인한다.
     * @param commentAnalysis 코멘트 분석 결과
     * @return null 이 아닌 코멘트 분석 결과
     */
    private QualitativeCommentAnalysis requireCommentAnalysis(QualitativeCommentAnalysis commentAnalysis) {
        if (commentAnalysis == null) {
            throw new IllegalStateException("Comment analysis is required for comment-based qualitative evaluation.");
        }
        return commentAnalysis;
    }
}
