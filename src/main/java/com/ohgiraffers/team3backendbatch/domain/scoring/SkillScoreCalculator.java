package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class SkillScoreCalculator {

    /**
     * 기간 기여점을 반영하여 현재 skill 점수를 이동시키는 계산기다.
     *
     * 구현 메모:
     * - 공식 현재 skill 은 MONTH settlement 결과로 갱신한다.
     * - WEEK preview 는 화면용 임시 결과만 만들고, 공식 current skill 을 직접 변경하지 않는다.
     * - QUARTER/HALF_YEAR/YEAR summary 는 월간 settlement 결과를 집계해 보여주되,
     *   current skill 갱신 정책은 별도로 분리해서 관리한다.
     *
     * 추후 추가 예정 메서드/내용:
     * - calculateMonthlySkillDelta(...)
     * - aggregateMonthlySkillForSummary(...)
     * - applyManualOverrideIfExists(...)
     */
    public BigDecimal calculateNextSkillScore(BigDecimal previousScore, int contributionPoint) {
        BigDecimal delta = BigDecimal.valueOf(contributionPoint - 5_000)
            .multiply(BigDecimal.valueOf(0.001));

        return previousScore.add(delta).setScale(2, RoundingMode.HALF_UP);
    }
}
