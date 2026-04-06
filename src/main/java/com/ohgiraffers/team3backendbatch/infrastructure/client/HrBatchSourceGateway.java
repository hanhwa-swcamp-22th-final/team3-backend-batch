package com.ohgiraffers.team3backendbatch.infrastructure.client;

/**
 * HR 서비스 연동 추상화 스켈레톤이다.
 *
 * 예상 기능:
 * - evaluation period 조회
 * - qualitative evaluation 원문 조회
 * - 재분석 대상 정성 평가 조회
 * - 승진 심사 진행 중 상태 확인
 *
 * 정성 평가 책임 분리 원칙:
 * - HR 는 평가 원문 작성/제출을 담당한다.
 * - Batch 는 원문을 읽어 NLP 기반 정성 점수 산정을 담당한다.
 */
public interface HrBatchSourceGateway {
    // TODO loadQualitativeEvaluationComments(...)
    // TODO loadEvaluationPeriod(...)
    // TODO loadPendingPromotionReviewStatus(...)
}