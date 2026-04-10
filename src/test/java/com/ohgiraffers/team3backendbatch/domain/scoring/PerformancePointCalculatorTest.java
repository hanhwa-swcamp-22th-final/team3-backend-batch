package com.ohgiraffers.team3backendbatch.domain.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PerformancePointCalculatorTest {

    private final PerformancePointCalculator performancePointCalculator = new PerformancePointCalculator();

    @Test
    @DisplayName("percentage scores use the sixty-point multiplier")
    void percentageToContributionPointUsesSixtyMultiplier() {
        assertThat(performancePointCalculator.percentageToContributionPoint(new BigDecimal("60.00"))).isEqualTo(3600);
        assertThat(performancePointCalculator.percentageToContributionPoint(new BigDecimal("80.00"))).isEqualTo(4800);
        assertThat(performancePointCalculator.percentageToContributionPoint(new BigDecimal("95.50"))).isEqualTo(5730);
    }

    @Test
    @DisplayName("ratio scores use the six-thousand-point scale")
    void ratioToContributionPointUsesSixThousandScale() {
        assertThat(performancePointCalculator.ratioToContributionPoint(new BigDecimal("0.60"))).isEqualTo(3600);
        assertThat(performancePointCalculator.ratioToContributionPoint(new BigDecimal("0.80"))).isEqualTo(4800);
        assertThat(performancePointCalculator.ratioToContributionPoint(BigDecimal.ONE)).isEqualTo(6000);
    }

    @Test
    @DisplayName("kms contribution retains the tiered bucket policy")
    void kmsContributionPointRetainsTieredBuckets() {
        assertThat(performancePointCalculator.kmsContributionPoint(0)).isEqualTo(0);
        assertThat(performancePointCalculator.kmsContributionPoint(1)).isEqualTo(4000);
        assertThat(performancePointCalculator.kmsContributionPoint(2)).isEqualTo(6500);
        assertThat(performancePointCalculator.kmsContributionPoint(3)).isEqualTo(8000);
        assertThat(performancePointCalculator.kmsContributionPoint(4)).isEqualTo(9000);
        assertThat(performancePointCalculator.kmsContributionPoint(5)).isEqualTo(10000);
    }

    @Test
    @DisplayName("challenge contribution uses stepped buckets for high-difficulty work")
    void challengeContributionPointUsesTieredBuckets() {
        assertThat(performancePointCalculator.challengeContributionPoint(0)).isEqualTo(0);
        assertThat(performancePointCalculator.challengeContributionPoint(1)).isEqualTo(3000);
        assertThat(performancePointCalculator.challengeContributionPoint(2)).isEqualTo(5000);
        assertThat(performancePointCalculator.challengeContributionPoint(3)).isEqualTo(7000);
        assertThat(performancePointCalculator.challengeContributionPoint(4)).isEqualTo(8500);
        assertThat(performancePointCalculator.challengeContributionPoint(5)).isEqualTo(10000);
    }
}
