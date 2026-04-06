package com.ohgiraffers.team3backendbatch.domain.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QualitativeScoreCalculatorTest {

    private final QualitativeScoreCalculator calculator = new QualitativeScoreCalculator();

    @Test
    void calculateWeightedAverage_gives_more_weight_to_contrastive_chunks() {
        BigDecimal raw = calculator.calculateWeightedAverage(List.of(
            new ChunkContribution(BigDecimal.valueOf(0.2), false),
            new ChunkContribution(BigDecimal.valueOf(0.8), true)
        ));

        assertThat(raw).isEqualByComparingTo("0.5600");
    }

    @Test
    void normalizeToSQual_uses_t_score_formula() {
        BigDecimal score = calculator.normalizeToSQual(BigDecimal.valueOf(1.95));

        assertThat(score).isEqualByComparingTo("89.00");
    }

    @Test
    void calculateSecondaryAdjustmentRaw_limits_secondary_adjustment_range() {
        BigDecimal adjustment = calculator.calculateSecondaryAdjustmentRaw(BigDecimal.valueOf(1.95));

        assertThat(adjustment).isEqualByComparingTo("0.5000");
    }
}