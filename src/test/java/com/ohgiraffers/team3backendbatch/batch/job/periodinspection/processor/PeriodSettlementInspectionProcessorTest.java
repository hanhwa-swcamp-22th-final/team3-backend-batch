package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionResult;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionTarget;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PeriodSettlementInspectionProcessorTest {

    private final PeriodSettlementInspectionProcessor processor = new PeriodSettlementInspectionProcessor();

    @Test
    @DisplayName("missing monthly settlements and large score swings are reported as warnings")
    void processWarnsWhenMonthlyDataIsIncompleteOrVolatile() throws Exception {
        PeriodSettlementInspectionTarget target = PeriodSettlementInspectionTarget.builder()
            .evaluationPeriodId(10L)
            .periodType(BatchPeriodType.QUARTER)
            .employeeId(100L)
            .expectedMonthCount(3)
            .quantitativeMonthCount(2)
            .qualitativeMonthCount(1)
            .quantitativeMinScore(new BigDecimal("40.00"))
            .quantitativeMaxScore(new BigDecimal("70.00"))
            .qualitativeMinScore(new BigDecimal("55.00"))
            .qualitativeMaxScore(new BigDecimal("85.00"))
            .performancePointTotal(BigDecimal.ZERO)
            .build();

        PeriodSettlementInspectionResult result = processor.process(target);

        assertThat(result.getInspectionStatus()).isEqualTo("WARN");
        assertThat(result.getFindings()).hasSize(5);
    }

    @Test
    @DisplayName("complete and stable monthly settlements pass inspection")
    void processPassesWhenMonthlyDataIsComplete() throws Exception {
        PeriodSettlementInspectionTarget target = PeriodSettlementInspectionTarget.builder()
            .evaluationPeriodId(20L)
            .periodType(BatchPeriodType.HALF_YEAR)
            .employeeId(200L)
            .expectedMonthCount(6)
            .quantitativeMonthCount(6)
            .qualitativeMonthCount(6)
            .quantitativeMinScore(new BigDecimal("61.00"))
            .quantitativeMaxScore(new BigDecimal("79.00"))
            .qualitativeMinScore(new BigDecimal("64.00"))
            .qualitativeMaxScore(new BigDecimal("81.00"))
            .performancePointTotal(new BigDecimal("12600"))
            .build();

        PeriodSettlementInspectionResult result = processor.process(target);

        assertThat(result.getInspectionStatus()).isEqualTo("OK");
        assertThat(result.getFindings()).isEmpty();
    }
}
