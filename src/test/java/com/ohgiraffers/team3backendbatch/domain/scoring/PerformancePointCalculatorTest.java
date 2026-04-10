package com.ohgiraffers.team3backendbatch.domain.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PerformancePointCalculatorTest {

    private final PerformancePointCalculator performancePointCalculator = new PerformancePointCalculator();

    @Test
    @DisplayName("정량/정성 점수는 60배 계수로 contribution point로 환산한다")
    void percentageToContributionPointUsesSixtyMultiplier() {
        assertThat(performancePointCalculator.percentageToContributionPoint(new BigDecimal("60.00"))).isEqualTo(3600);
        assertThat(performancePointCalculator.percentageToContributionPoint(new BigDecimal("80.00"))).isEqualTo(4800);
        assertThat(performancePointCalculator.percentageToContributionPoint(new BigDecimal("95.50"))).isEqualTo(5730);
    }

    @Test
    @DisplayName("비율 기반 점수는 6000 만점 기준으로 contribution point를 계산한다")
    void ratioToContributionPointUsesSixThousandScale() {
        assertThat(performancePointCalculator.ratioToContributionPoint(new BigDecimal("0.60"))).isEqualTo(3600);
        assertThat(performancePointCalculator.ratioToContributionPoint(new BigDecimal("0.80"))).isEqualTo(4800);
        assertThat(performancePointCalculator.ratioToContributionPoint(BigDecimal.ONE)).isEqualTo(6000);
    }

    @Test
    @DisplayName("KMS 기여점수는 기존 구간값을 유지한다")
    void kmsContributionPointRetainsTieredBuckets() {
        assertThat(performancePointCalculator.kmsContributionPoint(0)).isEqualTo(0);
        assertThat(performancePointCalculator.kmsContributionPoint(1)).isEqualTo(4000);
        assertThat(performancePointCalculator.kmsContributionPoint(2)).isEqualTo(6500);
        assertThat(performancePointCalculator.kmsContributionPoint(3)).isEqualTo(8000);
        assertThat(performancePointCalculator.kmsContributionPoint(4)).isEqualTo(9000);
        assertThat(performancePointCalculator.kmsContributionPoint(5)).isEqualTo(10000);
    }
}
