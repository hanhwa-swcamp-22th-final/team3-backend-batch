package com.ohgiraffers.team3backendbatch.domain.quantitative.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class QuantitativeScoreCalculatorTest {

    private final QuantitativeScoreCalculator calculator = new QuantitativeScoreCalculator(new ObjectMapper());

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
        BigDecimal etaMaint = calculator.calculateEtaMaint(nMaint);
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
        BigDecimal eIdx = calculator.calculateEIdx("B", nAge, etaAge, etaMaint, nEnv, materialShielding);
        BigDecimal adjustedBaselineError = calculator.calculateAdjustedBaselineError(baselineError, eIdx);
        BigDecimal bonusPoint = calculator.calculateBonusPoint(null, "D4", "B");
        BigDecimal effectiveActualError = calculator.calculateEffectiveActualError(actualError, materialShielding);
        BigDecimal provisionalSQuant = calculator.calculateProvisionalSQuantFromErrorRate(
            effectiveActualError,
            adjustedBaselineError,
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
        assertThat(etaMaint).isEqualByComparingTo("0.84");
        assertThat(nEnv).isEqualByComparingTo("0.13");
        assertThat(materialShielding).isEqualByComparingTo("1.00");
        assertThat(difficultyAdjustment).isEqualByComparingTo("1.05");
        assertThat(baselineError).isEqualByComparingTo("5.00");
        assertThat(qBase).isEqualByComparingTo("100.00");
        assertThat(eIdx).isEqualByComparingTo("1.00");
        assertThat(adjustedBaselineError).isEqualByComparingTo("5.00");
        assertThat(bonusPoint).isEqualByComparingTo("5.00");
        assertThat(effectiveActualError).isEqualByComparingTo("2.80");
        assertThat(provisionalSQuant).isEqualByComparingTo("51.20");
        assertThat(finalSQuant).isEqualByComparingTo("55.20");
        assertThat(calculator.calculateTScore(finalSQuant, BigDecimal.valueOf(80), BigDecimal.valueOf(10), BatchPeriodType.MONTH))
            .isEqualByComparingTo("25.20");
        assertThat(calculator.resolveStatus(BatchPeriodType.MONTH)).isEqualTo("CONFIRMED");
    }

    @Test
    void shouldKeepWeeklyResultAsPreviewWithoutMonthlyAdjustments() {
        BigDecimal qBase = calculator.calculateQBase(
            BigDecimal.valueOf(80),
            BigDecimal.valueOf(90),
            BigDecimal.valueOf(85)
        );
        BigDecimal provisionalSQuant = calculator.calculateProvisionalSQuantFromErrorRate(
            BigDecimal.valueOf(4),
            BigDecimal.ZERO,
            BigDecimal.ONE,
            BigDecimal.ZERO,
            qBase
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
        assertThat(calculator.resolveStatus(BatchPeriodType.WEEK)).isEqualTo("TEMPORARY");
    }

    @Test
    void shouldApplyBackLoadedExponentialDecayToAgeAndMaintenance() {
        BigDecimal nAge = calculator.calculateNAge(
            LocalDate.of(2021, 6, 1),
            LocalDate.of(2024, 6, 30),
            24,
            120
        );
        BigDecimal etaAge = calculator.calculateEtaAge(BigDecimal.valueOf(1.10), nAge);
        BigDecimal nMaint = calculator.calculateNMaint(BigDecimal.valueOf(164), BigDecimal.valueOf(2));
        BigDecimal etaMaint = calculator.calculateEtaMaint(nMaint);
        BigDecimal nEnv = calculator.calculateNEnv(
            BigDecimal.valueOf(27),
            BigDecimal.valueOf(20),
            BigDecimal.valueOf(26),
            BigDecimal.valueOf(61),
            BigDecimal.valueOf(40),
            BigDecimal.valueOf(60),
            BigDecimal.valueOf(110),
            BigDecimal.valueOf(100),
            null,
            null,
            null
        );
        BigDecimal eIdx = calculator.calculateEIdx("B", nAge, etaAge, etaMaint, nEnv, BigDecimal.ZERO);

        assertThat(nAge).isEqualByComparingTo("0.13");
        assertThat(etaAge).isEqualByComparingTo("0.97");
        assertThat(nMaint).isEqualByComparingTo("0.82");
        assertThat(etaMaint).isEqualByComparingTo("0.81");
        assertThat(nEnv).isEqualByComparingTo("0.11");
        assertThat(eIdx).isEqualByComparingTo("1.02");
    }

    @Test
    void shouldCalculateSQuantFromAdjustedBaselineAndBonusPoint() {
        BigDecimal adjustedBaselineError = calculator.calculateAdjustedBaselineError(
            calculator.calculateBaselineError(null, BigDecimal.valueOf(4.5), BigDecimal.valueOf(0.25)),
            BigDecimal.valueOf(1.05)
        );
        BigDecimal bonusPoint = calculator.calculateBonusPoint(null, "D4", "B");
        BigDecimal effectiveActualError = calculator.calculateEffectiveActualError(BigDecimal.valueOf(3.6), BigDecimal.ZERO);
        BigDecimal sQuant = calculator.calculateFinalSQuant(
            calculator.calculateProvisionalSQuantFromErrorRate(
                effectiveActualError,
                adjustedBaselineError,
                calculator.calculateDifficultyAdjustment(null, "D4"),
                bonusPoint,
                BigDecimal.ZERO
            ),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BatchPeriodType.MONTH
        );

        assertThat(adjustedBaselineError).isEqualByComparingTo("5.31");
        assertThat(bonusPoint).isEqualByComparingTo("5.00");
        assertThat(effectiveActualError).isEqualByComparingTo("3.60");
        assertThat(sQuant).isEqualByComparingTo("40.42");
    }

    @Test
    void shouldReduceActualErrorWhenMaterialShieldingIsTriggered() {
        BigDecimal withoutShielding = calculator.calculateEffectiveActualError(
            BigDecimal.valueOf(3.6),
            BigDecimal.ZERO
        );
        BigDecimal withShielding = calculator.calculateEffectiveActualError(
            BigDecimal.valueOf(3.6),
            BigDecimal.ONE
        );
        BigDecimal shieldingFlag = calculator.calculateMaterialShielding(3, 5, null);

        assertThat(shieldingFlag).isEqualByComparingTo("1.00");
        assertThat(withoutShielding).isEqualByComparingTo("3.60");
        assertThat(withShielding).isEqualByComparingTo("2.52");
    }
}
