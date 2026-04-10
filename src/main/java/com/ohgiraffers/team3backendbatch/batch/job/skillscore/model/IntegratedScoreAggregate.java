package com.ohgiraffers.team3backendbatch.batch.job.skillscore.model;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.SkillGrowthCalculatedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final BigDecimal quantitativeProductivityScore;
    private final BigDecimal quantitativeQualityScore;
    private final BigDecimal quantitativeEquipmentResponseScore;
    private final BigDecimal qualitativeScore;
    private final Map<String, BigDecimal> qualitativeSkillScores;
    private final Integer quantitativePoint;
    private final Integer qualitativePoint;
    private final List<PerformancePointCalculatedEvent> performancePointEvents;
    private final List<SkillGrowthCalculatedEvent> skillGrowthEvents;

    public IntegratedScoreAggregate withCalculatedResults(
        Integer quantitativePoint,
        Integer qualitativePoint,
        List<PerformancePointCalculatedEvent> performancePointEvents,
        List<SkillGrowthCalculatedEvent> skillGrowthEvents
    ) {
        return this.toBuilder()
            .quantitativePoint(quantitativePoint)
            .qualitativePoint(qualitativePoint)
            .qualitativeSkillScores(qualitativeSkillScores == null ? Map.of() : Map.copyOf(qualitativeSkillScores))
            .performancePointEvents(performancePointEvents == null ? List.of() : List.copyOf(performancePointEvents))
            .skillGrowthEvents(skillGrowthEvents == null ? List.of() : List.copyOf(skillGrowthEvents))
            .build();
    }
}
