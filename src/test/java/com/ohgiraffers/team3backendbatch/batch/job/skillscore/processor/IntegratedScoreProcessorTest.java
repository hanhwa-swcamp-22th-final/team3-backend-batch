package com.ohgiraffers.team3backendbatch.batch.job.skillscore.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.MonthlySkillContributionCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.TierAwareKpiScoreCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntegratedScoreProcessorTest {

    private final IntegratedScoreProcessor integratedScoreProcessor =
        new IntegratedScoreProcessor(
            new PerformancePointCalculator(),
            new MonthlySkillContributionCalculator(),
            new TierAwareKpiScoreCalculator()
        );

    @Test
    @DisplayName("monthly settlement includes tier-aware KPI, KMS, challenge, and skill growth")
    void processBuildsMonthlyEvents() {
        Map<String, BigDecimal> qualitativeSkillScores = new LinkedHashMap<>();
        qualitativeSkillScores.put("EQUIPMENT_RESPONSE", new BigDecimal("70.00"));
        qualitativeSkillScores.put("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        qualitativeSkillScores.put("INNOVATION_PROPOSAL", new BigDecimal("90.00"));
        qualitativeSkillScores.put("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        qualitativeSkillScores.put("QUALITY_MANAGEMENT", new BigDecimal("75.00"));
        qualitativeSkillScores.put("PRODUCTIVITY", new BigDecimal("66.00"));

        IntegratedScoreAggregate aggregate = IntegratedScoreAggregate.builder()
            .employeeId(101L)
            .employeeTier("A")
            .evaluationPeriodId(202604L)
            .periodType(BatchPeriodType.MONTH)
            .pointEarnedDate(LocalDate.of(2026, 4, 30))
            .occurredAt(LocalDateTime.of(2026, 4, 30, 23, 59))
            .quantitativeTScore(new BigDecimal("60.00"))
            .quantitativeProductivityScore(new BigDecimal("60.00"))
            .quantitativeQualityScore(new BigDecimal("80.00"))
            .quantitativeEquipmentResponseScore(new BigDecimal("50.00"))
            .qualitativeScore(new BigDecimal("80.00"))
            .qualitativeSkillScores(qualitativeSkillScores)
            .kmsApprovedArticleCount(2)
            .challengeTaskCount(3)
            .performancePointEvents(List.of())
            .build();

        IntegratedScoreAggregate result = integratedScoreProcessor.process(aggregate);

        assertThat(result).isNotNull();
        assertThat(result.getQuantitativePoint()).isEqualTo(3750);
        assertThat(result.getQualitativePoint()).isEqualTo(4800);
        assertThat(result.getPerformancePointEvents()).hasSize(4);
        assertThat(result.getPerformancePointEvents()).extracting("pointType")
            .containsExactly("QUANTITY", "QUALITATIVE", "KNOWLEDGE_SHARING", "CHALLENGE");
        assertThat(result.getPerformancePointEvents().get(2).getPointAmount()).isEqualByComparingTo("6500");
        assertThat(result.getPerformancePointEvents().get(3).getPointAmount()).isEqualByComparingTo("7000");
        assertThat(result.getSkillGrowthEvents()).hasSize(6);
        assertThat(result.getSkillGrowthEvents().get(0).getSkillCategory()).isEqualTo("EQUIPMENT_RESPONSE");
        assertThat(result.getSkillGrowthEvents().get(0).getSkillContributionScore()).isEqualByComparingTo("58.00");
    }

    @Test
    @DisplayName("weekly summary does not emit official performance point or skill growth events")
    void processSkipsOfficialUpdatesForWeeklySummary() {
        IntegratedScoreAggregate aggregate = IntegratedScoreAggregate.builder()
            .employeeId(101L)
            .employeeTier("B")
            .evaluationPeriodId(202615L)
            .periodType(BatchPeriodType.WEEK)
            .pointEarnedDate(LocalDate.of(2026, 4, 10))
            .occurredAt(LocalDateTime.of(2026, 4, 10, 10, 0))
            .quantitativeTScore(new BigDecimal("61.00"))
            .qualitativeScore(new BigDecimal("77.00"))
            .qualitativeSkillScores(Map.of("PRODUCTIVITY", new BigDecimal("70.00")))
            .performancePointEvents(List.of())
            .build();

        IntegratedScoreAggregate result = integratedScoreProcessor.process(aggregate);

        assertThat(result).isNotNull();
        assertThat(result.getPerformancePointEvents()).isEmpty();
        assertThat(result.getSkillGrowthEvents()).isEmpty();
        assertThat(result.getQuantitativePoint()).isNull();
        assertThat(result.getQualitativePoint()).isNull();
    }
}
