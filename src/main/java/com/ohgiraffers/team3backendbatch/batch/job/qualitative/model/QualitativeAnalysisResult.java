package com.ohgiraffers.team3backendbatch.batch.job.qualitative.model;

/**
 * 정성 평가 NLP 분석 결과 모델이다.
 *
 * 예상 필드:
 * - evaluationId
 * - squalRaw
 * - sQual
 * - normalizedTier
 * - matchedKeywordCount
 * - matchedKeywordsJson
 * - contextWeight
 * - negationDetected
 * - algorithmVersion
 * - analysisStatus
 * - analyzedAt
 *
 * 이 모델은 ScoreAggregationJob 에서 월간 정산 시 읽을 수 있는 정성 점수의 공식 결과를 의미한다.
 */
public class QualitativeAnalysisResult {
    // TODO 정성 점수 결과 필드 정의
}