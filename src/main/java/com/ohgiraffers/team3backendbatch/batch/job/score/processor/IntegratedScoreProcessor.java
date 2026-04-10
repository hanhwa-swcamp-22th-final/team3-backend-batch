package com.ohgiraffers.team3backendbatch.batch.job.score.processor;

import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public IntegratedScoreAggregate process(IntegratedScoreAggregate item) {
        Integer quantitativePoint = null;
        Integer qualitativePoint = null;
        List<PerformancePointCalculatedEvent> events = new ArrayList<>();

        if (item.getQuantitativeTScore() != null) {
            quantitativePoint = performancePointCalculator.percentageToContributionPoint(item.getQuantitativeTScore());
            events.add(buildEvent(item, QUANTITATIVE_POINT_TYPE, BigDecimal.valueOf(quantitativePoint), "Monthly quantitative settlement contribution"));
        }

        if (item.getQualitativeScore() != null) {
            qualitativePoint = performancePointCalculator.percentageToContributionPoint(item.getQualitativeScore());
            events.add(buildEvent(item, QUALITATIVE_POINT_TYPE, BigDecimal.valueOf(qualitativePoint), "Monthly qualitative settlement contribution"));
        }

        if (events.isEmpty()) {
            return null;
        }

        return item.withCalculatedPoints(quantitativePoint, qualitativePoint, events);
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
}