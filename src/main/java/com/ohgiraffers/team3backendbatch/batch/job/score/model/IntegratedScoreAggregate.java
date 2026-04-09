package com.ohgiraffers.team3backendbatch.batch.job.score.model;

/**
 * 통합 점수 계산 직전에 필요한 데이터를 모은 모델 스켈레톤이다.
 *
 * 예상 입력:
 * - periodType
 * - evaluationPeriodId
 * - quantitative evaluation 최종값
 * - qualitative evaluation 최종값
 * - KMS 승인 건수 또는 카테고리별 기여값
 * - 기존 score current 값
 * - 기존 skill current 값
 *
 * 예상 출력 대상:
 * - score
 * - skill
 * - performance_point
 */
public class IntegratedScoreAggregate {
    // TODO 직원별 통합 점수 계산 입력 필드 정의
}