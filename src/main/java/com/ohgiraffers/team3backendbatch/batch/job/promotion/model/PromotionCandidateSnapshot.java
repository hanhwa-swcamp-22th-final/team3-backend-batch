package com.ohgiraffers.team3backendbatch.batch.job.promotion.model;

/**
 * 승진 후보 판정 직전의 상태를 담는 모델 스켈레톤이다.
 *
 * 예상 필드:
 * - employeeId
 * - latestPeriodType
 * - latestEvaluationPeriodId
 * - currentTier
 * - targetTier
 * - tierAccumulatedPoint
 * - promotionThreshold
 * - hasPendingPromotion
 *
 * Reader가 이 모델을 만들고,
 * Processor가 후보 여부를 판정하며,
 * Writer가 promotion_history를 반영하는 흐름을 예상한다.
 */
public class PromotionCandidateSnapshot {
    // TODO 승진 후보 판정 입력 필드 정의
}