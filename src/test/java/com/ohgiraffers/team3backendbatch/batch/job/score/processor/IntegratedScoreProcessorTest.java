package com.ohgiraffers.team3backendbatch.batch.job.score.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntegratedScoreProcessorTest {

    private final IntegratedScoreProcessor integratedScoreProcessor =
        new IntegratedScoreProcessor(new PerformancePointCalculator());

    @Test
    @DisplayName("월간 정량/정성 점수를 승진 누적용 contribution point 이벤트로 변환한다")
    void processBuildsPerformancePointEventsFromMonthlyScores() throws Exception {
        IntegratedScoreAggregate aggregate = IntegratedScoreAggregate.builder()
            .employeeId(101L)
            .evaluationPeriodId(202604L)
            .periodType(BatchPeriodType.MONTH)
            .pointEarnedDate(LocalDate.of(2026, 4, 30))
            .occurredAt(LocalDateTime.of(2026, 4, 30, 23, 59))
            .quantitativeTScore(new BigDecimal("60.00"))
            .qualitativeScore(new BigDecimal("80.00"))
            .performancePointEvents(java.util.List.of())
            .build();

        IntegratedScoreAggregate result = integratedScoreProcessor.process(aggregate);

        assertThat(result).isNotNull();
        assertThat(result.getQuantitativePoint()).isEqualTo(3600);
        assertThat(result.getQualitativePoint()).isEqualTo(4800);
        assertThat(result.getPerformancePointEvents()).hasSize(2);

        assertThat(result.getPerformancePointEvents().get(0).getPointType()).isEqualTo("QUANTITY");
        assertThat(result.getPerformancePointEvents().get(0).getPointAmount()).isEqualByComparingTo("3600");
        assertThat(result.getPerformancePointEvents().get(1).getPointType()).isEqualTo("QUALITATIVE");
        assertThat(result.getPerformancePointEvents().get(1).getPointAmount()).isEqualByComparingTo("4800");
    }
}
