package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PerformancePointCalculator {

    private static final BigDecimal PERCENTAGE_POINT_MULTIPLIER = BigDecimal.valueOf(60);
    private static final BigDecimal RATIO_POINT_MULTIPLIER = BigDecimal.valueOf(6_000);

    /**
     * 퍼센트 기반 점수를 UI 표시용 기여점(0~10,000)으로 환산한다.
     *
     * 구현 메모:
     * - WEEK preview 와 MONTH settlement 모두 raw 원천 데이터를 contribution point 로 변환할 때 사용한다.
     * - QUARTER/HALF_YEAR/YEAR 는 월간 settlement 결과를 집계하는 summary 성격이므로
     *   raw 퍼센트 입력을 다시 이 메서드로 재계산하지 않는 것이 기본 원칙이다.
     *
     * 추후 추가 예정 메서드/내용:
     * - calculateWeeklyPreviewContribution(...)
     * - calculateMonthlySettlementContribution(...)
     * - aggregateMonthlyContributionForSummary(...)
     */
    public int percentageToContributionPoint(BigDecimal percentageScore) {
        return percentageScore
            .multiply(PERCENTAGE_POINT_MULTIPLIER)
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
    }

    /**
     * 0~1 범위의 달성 비율 데이터를 기여점(0~10,000)으로 환산한다.
     *
     * 구현 메모:
     * - UPH, 수율, 리드타임 보정값 같은 raw 정량 원천을 WEEK/MONTH 계산에 사용할 때 적합하다.
     * - 상위 기간 summary 는 월간 settlement 결과 집계를 우선한다.
     */
    public int ratioToContributionPoint(BigDecimal achievementRatio) {
        return achievementRatio
            .multiply(RATIO_POINT_MULTIPLIER)
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
    }

    /**
     * 승인된 KMS 문서 수를 기여점으로 변환한다.
     *
     * 구현 메모:
     * - 월간 settlement 에서 공식 반영하는 것이 기본이다.
     * - 주간 preview 에서 보여주더라도 공식 누적 점수 반영 시에는 월간 확정 로직을 따라야 한다.
     */
    public int kmsContributionPoint(int approvedArticleCount) {
        if (approvedArticleCount <= 0) {
            return 0;
        }
        if (approvedArticleCount == 1) {
            return 4_000;
        }
        if (approvedArticleCount == 2) {
            return 6_500;
        }
        if (approvedArticleCount == 3) {
            return 8_000;
        }
        if (approvedArticleCount == 4) {
            return 9_000;
        }
        return 10_000;
    }
}
