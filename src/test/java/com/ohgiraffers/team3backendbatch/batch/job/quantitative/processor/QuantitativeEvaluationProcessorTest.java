package com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.domain.quantitative.scoring.QuantitativeScoreCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class QuantitativeEvaluationProcessorTest {

    private final QuantitativeEvaluationProcessor processor =
        new QuantitativeEvaluationProcessor(new QuantitativeScoreCalculator(new ObjectMapper()));

    @Test
    void process_shouldPopulateCalculatedFields() throws Exception {
        QuantitativeEvaluationAggregate item = QuantitativeEvaluationAggregate.builder()
            .employeeId(101L)
            .evaluationPeriodId(202601L)
            .equipmentId(7L)
            .periodType(BatchPeriodType.MONTH)
            .algorithmVersionId(1L)
            .totalInputQty(BigDecimal.valueOf(200))
            .totalGoodQty(BigDecimal.valueOf(192))
            .totalDefectQty(BigDecimal.valueOf(8))
            .averageLeadTimeSec(BigDecimal.valueOf(60))
            .targetUph(BigDecimal.valueOf(50))
            .targetYieldRate(BigDecimal.valueOf(95))
            .targetLeadTimeSec(BigDecimal.valueOf(75))
            .difficultyGrade("D4")
            .currentSkillTier("B")
            .baselineError(BigDecimal.valueOf(5))
            .antiGamingPenalty(BigDecimal.ONE)
            .build();

        QuantitativeEvaluationAggregate result = processor.process(item);

        assertThat(result.getActualError()).isEqualByComparingTo("4.00");
        assertThat(result.getNAge()).isEqualByComparingTo("0.00");
        assertThat(result.getEtaAge()).isEqualByComparingTo("1.00");
        assertThat(result.getNMaint()).isEqualByComparingTo("1.00");
        assertThat(result.getEtaMaint()).isEqualByComparingTo("1.00");
        assertThat(result.getNEnv()).isEqualByComparingTo("0.00");
        assertThat(result.getMaterialShielding()).isEqualByComparingTo("0.00");
        assertThat(result.getUphScore()).isEqualByComparingTo("100.00");
        assertThat(result.getYieldScore()).isEqualByComparingTo("100.00");
        assertThat(result.getLeadTimeScore()).isEqualByComparingTo("100.00");
        assertThat(result.getDifficultyAdjustment()).isEqualByComparingTo("1.10");
        assertThat(result.getQBase()).isEqualByComparingTo("100.00");
        assertThat(result.getEIdx()).isEqualByComparingTo("1.00");
        assertThat(result.getBonusPoint()).isEqualByComparingTo("5.00");
        assertThat(result.getProvisionalSQuant()).isEqualByComparingTo("27.00");
        assertThat(result.getSQuant()).isEqualByComparingTo("26.00");
        assertThat(result.getTScore()).isEqualByComparingTo("26.00");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }
}
