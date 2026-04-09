package com.ohgiraffers.team3backendbatch.domain.quantitative.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class QuantitativeScoreCalculatorTest {

    private final QuantitativeScoreCalculator calculator = new QuantitativeScoreCalculator();

    @Test
    void shouldCalculateMonthlySettlementScoresFromStagedInputs() {
        BigDecimal actualError = calculator.resolveActualError(
            null,
            BigDecimal.valueOf(8),
            BigDecimal.valueOf(200)
        );
        BigDecimal uphScore = calculator.calculateUphScore(BigDecimal.valueOf(60), BigDecimal.valueOf(50));
        BigDecimal yieldScore = calculator.calculateYieldScore(
            BigDecimal.valueOf(192),
            BigDecimal.valueOf(200),
            BigDecimal.valueOf(95)
        );
        BigDecimal leadTimeScore = calculator.calculateLeadTimeScore(BigDecimal.valueOf(60), BigDecimal.valueOf(75));
        BigDecimal nAge = calculator.calculateNAge(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 30),
            12,
            60
        );
        BigDecimal etaAge = calculator.calculateEtaAge(new BigDecimal("0.20"), nAge);
        BigDecimal nMaint = calculator.calculateNMaint(BigDecimal.valueOf(170), BigDecimal.valueOf(2));
        BigDecimal nEnv = calculator.calculateNEnv(
            BigDecimal.valueOf(28),
            BigDecimal.valueOf(20),
            BigDecimal.valueOf(26),
            BigDecimal.valueOf(55),
            BigDecimal.valueOf(40),
            BigDecimal.valueOf(60),
            BigDecimal.valueOf(90),
            BigDecimal.valueOf(100),
            null,
            null,
            null
        );
        BigDecimal materialShielding = calculator.calculateMaterialShielding(4, 5, null);
        BigDecimal difficultyAdjustment = calculator.calculateDifficultyAdjustment(null, "D3");
        BigDecimal baselineError = calculator.calculateBaselineError(null, BigDecimal.valueOf(5), nAge);
        BigDecimal qBase = calculator.calculateQBase(uphScore, yieldScore, leadTimeScore);
        BigDecimal eIdx = calculator.calculateEIdx("B", nAge, etaAge, nMaint, nEnv, materialShielding);
        BigDecimal bonusPoint = calculator.calculateBonusPoint(null, "D4", "B");
        BigDecimal provisionalSQuant = calculator.calculateProvisionalSQuantFromErrorRate(
            actualError,
            baselineError,
            difficultyAdjustment,
            bonusPoint,
            qBase
        );
        BigDecimal finalSQuant = calculator.calculateFinalSQuant(
            provisionalSQuant,
            BigDecimal.valueOf(3),
            BigDecimal.valueOf(2),
            BigDecimal.ONE,
            BatchPeriodType.MONTH
        );

        assertThat(actualError).isEqualByComparingTo("4.00");
        assertThat(nAge).isEqualByComparingTo("0.00");
        assertThat(etaAge).isEqualByComparingTo("1.00");
        assertThat(nMaint).isEqualByComparingTo("0.85");
        assertThat(nEnv).isEqualByComparingTo("0.13");
        assertThat(materialShielding).isEqualByComparingTo("1.00");
        assertThat(difficultyAdjustment).isEqualByComparingTo("1.05");
        assertThat(baselineError).isEqualByComparingTo("5.00");
        assertThat(qBase).isEqualByComparingTo("100.00");
        assertThat(eIdx).isEqualByComparingTo("1.00");
        assertThat(bonusPoint).isEqualByComparingTo("5.00");
        assertThat(provisionalSQuant).isEqualByComparingTo("26.00");
        assertThat(finalSQuant).isEqualByComparingTo("30.00");
        assertThat(calculator.calculateTScore(finalSQuant, BigDecimal.valueOf(80), BigDecimal.valueOf(10), BatchPeriodType.MONTH))
            .isEqualByComparingTo("0.00");
        assertThat(calculator.resolveStatus(BatchPeriodType.MONTH)).isEqualTo("SETTLED");
    }

    @Test
    void shouldKeepWeeklyResultAsPreviewWithoutMonthlyAdjustments() {
        BigDecimal qBase = calculator.calculateQBase(
            BigDecimal.valueOf(80),
            BigDecimal.valueOf(90),
            BigDecimal.valueOf(85)
        );
        BigDecimal provisionalSQuant = calculator.calculateProvisionalSQuant(
            qBase,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ZERO
        );
        BigDecimal finalSQuant = calculator.calculateFinalSQuant(
            provisionalSQuant,
            BigDecimal.valueOf(5),
            BigDecimal.valueOf(3),
            BigDecimal.valueOf(2),
            BatchPeriodType.WEEK
        );

        assertThat(qBase).isEqualByComparingTo("85.50");
        assertThat(provisionalSQuant).isEqualByComparingTo("85.50");
        assertThat(finalSQuant).isEqualByComparingTo("85.50");
        assertThat(calculator.calculateTScore(finalSQuant, BigDecimal.valueOf(75), BigDecimal.valueOf(8), BatchPeriodType.WEEK)).isNull();
        assertThat(calculator.resolveStatus(BatchPeriodType.WEEK)).isEqualTo("PREVIEW");
    }
}
