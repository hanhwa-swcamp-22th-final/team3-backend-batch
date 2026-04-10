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
    @DisplayName("default tier skill contribution mixes quantitative, qualitative, KMS, and challenge signals")
    void calculateMonthlySkillContributionsWithDefaultTierSignals() {
        Map<String, BigDecimal> qualitativeSkillScores = new LinkedHashMap<>();
        qualitativeSkillScores.put("EQUIPMENT_RESPONSE", new BigDecimal("70.00"));
        qualitativeSkillScores.put("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        qualitativeSkillScores.put("INNOVATION_PROPOSAL", new BigDecimal("90.00"));
        qualitativeSkillScores.put("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        qualitativeSkillScores.put("QUALITY_MANAGEMENT", new BigDecimal("75.00"));
        qualitativeSkillScores.put("PRODUCTIVITY", new BigDecimal("66.00"));

        Map<String, BigDecimal> result = calculator.calculateMonthlySkillContributions(
            "B",
            new BigDecimal("60.00"),
            new BigDecimal("80.00"),
            new BigDecimal("50.00"),
            new BigDecimal("80.00"),
            new BigDecimal("60.00"),
            qualitativeSkillScores
        );

        assertThat(result).containsEntry("EQUIPMENT_RESPONSE", new BigDecimal("66.00"));
        assertThat(result).containsEntry("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        assertThat(result).containsEntry("INNOVATION_PROPOSAL", new BigDecimal("79.00"));
        assertThat(result).containsEntry("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        assertThat(result).containsEntry("QUALITY_MANAGEMENT", new BigDecimal("79.00"));
        assertThat(result).containsEntry("PRODUCTIVITY", new BigDecimal("61.80"));
    }

    @Test
    @DisplayName("strategic tiers downweight raw productivity and boost reliability and knowledge signals")
    void calculateMonthlySkillContributionsWithStrategicTierSignals() {
        Map<String, BigDecimal> qualitativeSkillScores = new LinkedHashMap<>();
        qualitativeSkillScores.put("EQUIPMENT_RESPONSE", new BigDecimal("70.00"));
        qualitativeSkillScores.put("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        qualitativeSkillScores.put("INNOVATION_PROPOSAL", new BigDecimal("90.00"));
        qualitativeSkillScores.put("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        qualitativeSkillScores.put("QUALITY_MANAGEMENT", new BigDecimal("75.00"));
        qualitativeSkillScores.put("PRODUCTIVITY", new BigDecimal("66.00"));

        Map<String, BigDecimal> result = calculator.calculateMonthlySkillContributions(
            "S",
            new BigDecimal("60.00"),
            new BigDecimal("80.00"),
            new BigDecimal("50.00"),
            new BigDecimal("80.00"),
            new BigDecimal("60.00"),
            qualitativeSkillScores
        );

        assertThat(result).containsEntry("EQUIPMENT_RESPONSE", new BigDecimal("65.50"));
        assertThat(result).containsEntry("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        assertThat(result).containsEntry("INNOVATION_PROPOSAL", new BigDecimal("75.50"));
        assertThat(result).containsEntry("QUALITY_MANAGEMENT", new BigDecimal("78.95"));
        assertThat(result).containsEntry("PRODUCTIVITY", new BigDecimal("61.71"));
    }
}
