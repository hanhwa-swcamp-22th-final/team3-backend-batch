package com.ohgiraffers.team3backendbatch.batch.job.qualitative.model;

/**
 * 정성 평가 점수 계산 직전의 입력 모델이다.
 *
 * 예상 필드:
 * - evaluationId
 * - evaluationPeriodId
 * - employeeId
 * - evaluatorId
 * - commentText
 * - inputMethod
 * - analysisVersion
 * - submittedAt
 *
 * 이 모델은 HR 서비스가 저장한 원문 평가 데이터를 Batch 내부 정성 점수 계산 흐름으로 넘기기 위한 중간 객체다.
 */
public class QualitativeEvaluationAggregate {
    // TODO 정성 평가 원문 및 메타데이터 필드 정의
}