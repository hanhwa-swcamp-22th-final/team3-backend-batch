package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class TierScoreCalculator {

    /**
     * 기간 기여점을 tier 누적 점수 변화량으로 환산하는 계산기다.
     *
     * 구현 메모:
     * - 공식 누적 tier 점수는 MONTH settlement 결과를 기준으로 갱신한다.
     * - WEEK preview 는 승진/누적 판정에 직접 반영하지 않는다.
     * - QUARTER/HALF_YEAR/YEAR summary 는 월간 settlement 결과를 집계해서 보여주고,
     *   승진 판정은 이 공식 누적값만 사용한다.
     *
     * 추후 추가 예정 메서드/내용:
     * - calculateMonthlyTierDelta(...)
     * - aggregateMonthlyTierForSummary(...)
     * - applyTierConfigAdjustment(...)
     */
    public BigDecimal calculateNextTierScore(BigDecimal previousTierScore, int contributionPoint) {
        BigDecimal delta = BigDecimal.valueOf(contributionPoint - 5_000)
            .multiply(BigDecimal.valueOf(0.001));

        return previousTierScore.add(delta).setScale(2, RoundingMode.HALF_UP);
    }
}
