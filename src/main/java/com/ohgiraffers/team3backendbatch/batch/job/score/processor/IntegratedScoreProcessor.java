package com.ohgiraffers.team3backendbatch.batch.job.score.processor;

import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 정량/정성/KMS 데이터를 통합해 score, skill, performance_point 갱신 값을 만드는 Processor 스켈레톤이다.
 *
 * 구현 예정 메서드/내용:
 * - buildMonthlySettlementResult(...)
 * - buildUpperPeriodSummaryResult(...)
 * - buildPreviewOnlyResult(...)
 * - calculateCapabilityIndex(...)
 * - calculateTotalPoints(...)
 * - buildPerformanceHistory(...)
 *
 * 주의:
 * - WEEK preview 결과는 공식 current score/skill 누적 로직에 합산하지 않는다.
 * - QUARTER/HALF_YEAR/YEAR 는 raw `mes_*` 재계산이 아니라 월간 settlement 결과를 집계한다.
 */
@Component
public class IntegratedScoreProcessor
    implements ItemProcessor<IntegratedScoreAggregate, IntegratedScoreAggregate> {

    @Override
    public IntegratedScoreAggregate process(IntegratedScoreAggregate item) {
        // TODO score, skill, performance_point 결과 모델 생성
        return item;
    }
}