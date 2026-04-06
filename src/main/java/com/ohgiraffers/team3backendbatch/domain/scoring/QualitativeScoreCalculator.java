package com.ohgiraffers.team3backendbatch.domain.scoring;

import org.springframework.stereotype.Component;

/**
 * 정성 평가 원문으로부터 S_qual 을 계산하는 도메인 계산기 스켈레톤이다.
 *
 * 책임:
 * - 청크 점수 계산
 * - contrastive 청크 가중치 적용
 * - 키워드 가중치 합산
 * - NEG 보정
 * - Squal_raw 계산
 * - T-Score 또는 0~100 정규화 결과 산출
 */
@Component
public class QualitativeScoreCalculator {

    // TODO calculateChunkScore(...)
    // TODO calculateWeightedAverage(...)
    // TODO normalizeToSQual(...)
}