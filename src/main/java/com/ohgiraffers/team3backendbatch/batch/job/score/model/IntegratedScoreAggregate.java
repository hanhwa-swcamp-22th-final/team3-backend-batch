package com.ohgiraffers.team3backendbatch.batch.job.score.model;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class IntegratedScoreAggregate {

    private final Long employeeId;
    private final Long evaluationPeriodId;
    private final BatchPeriodType periodType;
    private final LocalDate pointEarnedDate;
    private final LocalDateTime occurredAt;
    private final BigDecimal quantitativeTScore;
    private final BigDecimal qualitativeScore;
    private final Integer quantitativePoint;
    private final Integer qualitativePoint;
    private final List<PerformancePointCalculatedEvent> performancePointEvents;

    public IntegratedScoreAggregate withCalculatedPoints(
        Integer quantitativePoint,
        Integer qualitativePoint,
        List<PerformancePointCalculatedEvent> performancePointEvents
    ) {
        return this.toBuilder()
            .quantitativePoint(quantitativePoint)
            .qualitativePoint(qualitativePoint)
            .performancePointEvents(performancePointEvents == null ? List.of() : List.copyOf(performancePointEvents))
            .build();
    }
}