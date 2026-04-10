package com.ohgiraffers.team3backendbatch.batch.job.skillscore.processor;

import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.MonthlySkillContributionCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.SkillGrowthCalculatedEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegratedScoreProcessor
    implements ItemProcessor<IntegratedScoreAggregate, IntegratedScoreAggregate> {

    private static final String POINT_SOURCE_TYPE = "EVALUATION_PERIOD_SETTLEMENT";
    private static final String QUANTITATIVE_POINT_TYPE = "QUANTITY";
    private static final String QUALITATIVE_POINT_TYPE = "QUALITATIVE";

    private final PerformancePointCalculator performancePointCalculator;
    private final MonthlySkillContributionCalculator monthlySkillContributionCalculator;

    @Override
    public IntegratedScoreAggregate process(IntegratedScoreAggregate item) {
        Integer quantitativePoint = null;
        Integer qualitativePoint = null;
        List<PerformancePointCalculatedEvent> events = new ArrayList<>();
        List<SkillGrowthCalculatedEvent> skillGrowthEvents = new ArrayList<>();

        if (item.getQuantitativeTScore() != null) {
            quantitativePoint = performancePointCalculator.percentageToContributionPoint(item.getQuantitativeTScore());
            events.add(buildEvent(item, QUANTITATIVE_POINT_TYPE, BigDecimal.valueOf(quantitativePoint), "Monthly quantitative settlement contribution"));
        }

        if (item.getQualitativeScore() != null) {
            qualitativePoint = performancePointCalculator.percentageToContributionPoint(item.getQualitativeScore());
            events.add(buildEvent(item, QUALITATIVE_POINT_TYPE, BigDecimal.valueOf(qualitativePoint), "Monthly qualitative settlement contribution"));
        }

        for (Map.Entry<String, BigDecimal> entry : monthlySkillContributionCalculator
            .calculateMonthlySkillContributions(
                item.getQuantitativeProductivityScore(),
                item.getQuantitativeQualityScore(),
                item.getQuantitativeEquipmentResponseScore(),
                item.getQualitativeSkillScores()
            )
            .entrySet()) {
            skillGrowthEvents.add(buildSkillGrowthEvent(item, entry.getKey(), entry.getValue()));
        }

        if (events.isEmpty() && skillGrowthEvents.isEmpty()) {
            return null;
        }

        return item.withCalculatedResults(quantitativePoint, qualitativePoint, events, skillGrowthEvents);
    }

    private PerformancePointCalculatedEvent buildEvent(
        IntegratedScoreAggregate item,
        String pointType,
        BigDecimal pointAmount,
        String description
    ) {
        return PerformancePointCalculatedEvent.builder()
            .employeeId(item.getEmployeeId())
            .evaluationPeriodId(item.getEvaluationPeriodId())
            .periodType(item.getPeriodType().name())
            .pointType(pointType)
            .pointAmount(pointAmount)
            .pointEarnedDate(item.getPointEarnedDate())
            .pointSourceId(item.getEvaluationPeriodId())
            .pointSourceType(POINT_SOURCE_TYPE)
            .pointDescription(description)
            .occurredAt(item.getOccurredAt())
            .build();
    }

    private SkillGrowthCalculatedEvent buildSkillGrowthEvent(
        IntegratedScoreAggregate item,
        String skillCategory,
        BigDecimal contributionScore
    ) {
        return SkillGrowthCalculatedEvent.builder()
            .employeeId(item.getEmployeeId())
            .evaluationPeriodId(item.getEvaluationPeriodId())
            .periodType(item.getPeriodType().name())
            .skillCategory(skillCategory)
            .skillContributionScore(contributionScore)
            .alpha(monthlySkillContributionCalculator.getDefaultAlpha())
            .contributionDate(item.getPointEarnedDate())
            .sourceId(item.getEvaluationPeriodId())
            .sourceType(POINT_SOURCE_TYPE)
            .occurredAt(item.getOccurredAt())
            .build();
    }
}
