package com.ohgiraffers.team3backendbatch.domain.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthlySkillContributionCalculatorTest {

    private final MonthlySkillContributionCalculator calculator = new MonthlySkillContributionCalculator();

    @Test
    @DisplayName("정량 세부축과 정성 카테고리 점수를 함께 반영한다")
    void calculateMonthlySkillContributionsWithPartialSignals() {
        Map<String, BigDecimal> qualitativeSkillScores = new LinkedHashMap<>();
        qualitativeSkillScores.put("EQUIPMENT_RESPONSE", new BigDecimal("70.00"));
        qualitativeSkillScores.put("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        qualitativeSkillScores.put("INNOVATION_PROPOSAL", new BigDecimal("90.00"));
        qualitativeSkillScores.put("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        qualitativeSkillScores.put("QUALITY_MANAGEMENT", new BigDecimal("75.00"));
        qualitativeSkillScores.put("PRODUCTIVITY", new BigDecimal("66.00"));

        Map<String, BigDecimal> result = calculator.calculateMonthlySkillContributions(
            new BigDecimal("60.00"),
            new BigDecimal("80.00"),
            new BigDecimal("50.00"),
            qualitativeSkillScores
        );

        assertThat(result).containsEntry("EQUIPMENT_RESPONSE", new BigDecimal("62.50"));
        assertThat(result).containsEntry("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        assertThat(result).containsEntry("INNOVATION_PROPOSAL", new BigDecimal("90.00"));
        assertThat(result).containsEntry("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        assertThat(result).containsEntry("QUALITY_MANAGEMENT", new BigDecimal("78.75"));
        assertThat(result).containsEntry("PRODUCTIVITY", new BigDecimal("61.80"));
    }

    @Test
    @DisplayName("정성 카테고리가 없으면 사용 가능한 정량 세부축만 반영한다")
    void calculateMonthlySkillContributionsWithOnlyQuantitativeSignals() {
        Map<String, BigDecimal> result = calculator.calculateMonthlySkillContributions(
            new BigDecimal("55.00"),
            new BigDecimal("72.00"),
            new BigDecimal("40.00"),
            Map.of()
        );

        assertThat(result).containsEntry("EQUIPMENT_RESPONSE", new BigDecimal("40.00"));
        assertThat(result).containsEntry("QUALITY_MANAGEMENT", new BigDecimal("72.00"));
        assertThat(result).containsEntry("PRODUCTIVITY", new BigDecimal("55.00"));
        assertThat(result).doesNotContainKeys("TECHNICAL_TRANSFER", "INNOVATION_PROPOSAL", "SAFETY_COMPLIANCE");
    }
}