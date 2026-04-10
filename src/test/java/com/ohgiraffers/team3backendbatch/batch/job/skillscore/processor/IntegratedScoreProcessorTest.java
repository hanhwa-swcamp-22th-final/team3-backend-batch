package com.ohgiraffers.team3backendbatch.batch.job.skillscore.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.MonthlySkillContributionCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
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
        new IntegratedScoreProcessor(new PerformancePointCalculator(), new MonthlySkillContributionCalculator());

    @Test
    @DisplayName("월간 평가 점수를 성과포인트와 skill growth 이벤트로 변환한다")
    void processBuildsPerformancePointEventsAndSkillEvents() throws Exception {
        Map<String, BigDecimal> qualitativeSkillScores = new LinkedHashMap<>();
        qualitativeSkillScores.put("EQUIPMENT_RESPONSE", new BigDecimal("70.00"));
        qualitativeSkillScores.put("TECHNICAL_TRANSFER", new BigDecimal("80.00"));
        qualitativeSkillScores.put("INNOVATION_PROPOSAL", new BigDecimal("90.00"));
        qualitativeSkillScores.put("SAFETY_COMPLIANCE", new BigDecimal("65.00"));
        qualitativeSkillScores.put("QUALITY_MANAGEMENT", new BigDecimal("75.00"));
        qualitativeSkillScores.put("PRODUCTIVITY", new BigDecimal("66.00"));

        IntegratedScoreAggregate aggregate = IntegratedScoreAggregate.builder()
            .employeeId(101L)
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
            .performancePointEvents(List.of())
            .build();

        IntegratedScoreAggregate result = integratedScoreProcessor.process(aggregate);

        assertThat(result).isNotNull();
        assertThat(result.getQuantitativePoint()).isEqualTo(3600);
        assertThat(result.getQualitativePoint()).isEqualTo(4800);
        assertThat(result.getPerformancePointEvents()).hasSize(2);
        assertThat(result.getSkillGrowthEvents()).hasSize(6);

        assertThat(result.getPerformancePointEvents().get(0).getPointType()).isEqualTo("QUANTITY");
        assertThat(result.getPerformancePointEvents().get(0).getPointAmount()).isEqualByComparingTo("3600");
        assertThat(result.getPerformancePointEvents().get(1).getPointType()).isEqualTo("QUALITATIVE");
        assertThat(result.getPerformancePointEvents().get(1).getPointAmount()).isEqualByComparingTo("4800");
        assertThat(result.getSkillGrowthEvents().get(0).getSkillCategory()).isEqualTo("EQUIPMENT_RESPONSE");
        assertThat(result.getSkillGrowthEvents().get(0).getSkillContributionScore()).isEqualByComparingTo("62.50");
    }
}
