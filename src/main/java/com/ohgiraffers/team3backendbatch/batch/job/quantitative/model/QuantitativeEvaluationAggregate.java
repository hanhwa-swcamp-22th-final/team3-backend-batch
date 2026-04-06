package com.ohgiraffers.team3backendbatch.batch.job.quantitative.model;

/**
 * 정량 평가 계산 직전의 집계 결과를 담는 모델 스켈레톤이다.
 *
 * 예상 필드:
 * - employeeId
 * - equipmentId
 * - periodType
 * - evaluationPeriodId
 * - algorithmVersionId
 * - totalInputQty
 * - totalGoodQty
 * - totalDefectQty
 * - averageLeadTimeSec
 * - downtimeMinutes
 * - maintenanceMinutes
 * - baselineError
 * - actualError
 * - eIdx
 *
 * 이 객체는 Reader가 periodType + evaluationPeriodId 기준으로 원천 집계값을 묶어 만들고,
 * Processor가 점수 계산을 수행하며,
 * Writer가 quantitative_evaluation 테이블로 반영하는 흐름의 중간 산출물이다.
 */
public class QuantitativeEvaluationAggregate {
    // TODO 집계 필드 정의 및 계산 편의 메서드 추가
}