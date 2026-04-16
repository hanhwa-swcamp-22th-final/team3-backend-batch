package com.ohgiraffers.team3backendbatch.batch.job.quantitative.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationSourceRow;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor.QuantitativeEvaluationProcessor;
import com.ohgiraffers.team3backendbatch.domain.quantitative.scoring.QuantitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.QuantitativeEvaluationQueryMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuantitativeSourceReaderTest {

    @Mock
    private QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper;

    @Mock
    private EvaluationPeriodQueryMapper evaluationPeriodQueryMapper;

    private final QuantitativeEvaluationProcessor quantitativeEvaluationProcessor =
        new QuantitativeEvaluationProcessor(new QuantitativeScoreCalculator(new ObjectMapper()));

    @Test
    void read_shouldMapSourceRowsToAggregate() throws Exception {
        QuantitativeEvaluationSourceRow row = new QuantitativeEvaluationSourceRow();
        row.setEmployeeId(10L);
        row.setEvaluationPeriodId(202601L);
        row.setEquipmentId(3L);
        row.setAlgorithmVersionId(11L);
        row.setEvaluationPeriodEndDate(LocalDate.of(2026, 1, 31));
        row.setTotalInputQty(BigDecimal.valueOf(200));
        row.setTotalGoodQty(BigDecimal.valueOf(190));
        row.setTotalDefectQty(BigDecimal.TEN);
        row.setAverageLeadTimeSec(BigDecimal.valueOf(61));
        row.setTargetUph(BigDecimal.valueOf(55));
        row.setTargetYieldRate(BigDecimal.valueOf(95));
        row.setTargetLeadTimeSec(BigDecimal.valueOf(70));
        row.setEquipmentGrade("B");
        row.setEquipmentWearCoefficient(BigDecimal.valueOf(0.2));
        row.setDifficultyScore(BigDecimal.valueOf(2));

        when(quantitativeEvaluationQueryMapper.findQuantitativeSourcesForEvaluation(202601L, 10L, false))
            .thenReturn(List.of(row));

        QuantitativeSourceReader reader = new QuantitativeSourceReader(
            quantitativeEvaluationQueryMapper,
            quantitativeEvaluationProcessor,
            evaluationPeriodQueryMapper,
            202601L,
            10L,
            "false",
            "MONTH"
        );

        QuantitativeEvaluationAggregate first = reader.read();
        QuantitativeEvaluationAggregate second = reader.read();

        assertThat(first).isNotNull();
        assertThat(first.getEmployeeId()).isEqualTo(10L);
        assertThat(first.getEvaluationPeriodId()).isEqualTo(202601L);
        assertThat(first.getEquipmentId()).isEqualTo(3L);
        assertThat(first.getPeriodType()).isEqualTo(BatchPeriodType.MONTH);
        assertThat(first.getTargetUph()).isEqualByComparingTo("55");
        assertThat(first.getDifficultyScore()).isEqualByComparingTo("2");
        assertThat(second).isNull();
    }

    @Test
    void read_shouldPopulateMonthlyGroupStatisticsForSameTier() throws Exception {
        QuantitativeEvaluationSourceRow firstRow = new QuantitativeEvaluationSourceRow();
        firstRow.setEmployeeId(10L);
        firstRow.setEvaluationPeriodId(202601L);
        firstRow.setEquipmentId(3L);
        firstRow.setAlgorithmVersionId(11L);
        firstRow.setTotalInputQty(BigDecimal.valueOf(100));
        firstRow.setTotalGoodQty(BigDecimal.valueOf(96));
        firstRow.setTotalDefectQty(BigDecimal.valueOf(4));
        firstRow.setDifficultyGrade("D2");
        firstRow.setCurrentSkillTier("A");
        firstRow.setBaselineError(BigDecimal.valueOf(5));
        firstRow.setEquipmentGrade("A");

        QuantitativeEvaluationSourceRow secondRow = new QuantitativeEvaluationSourceRow();
        secondRow.setEmployeeId(11L);
        secondRow.setEvaluationPeriodId(202601L);
        secondRow.setEquipmentId(4L);
        secondRow.setAlgorithmVersionId(11L);
        secondRow.setTotalInputQty(BigDecimal.valueOf(100));
        secondRow.setTotalGoodQty(BigDecimal.valueOf(98));
        secondRow.setTotalDefectQty(BigDecimal.valueOf(2));
        secondRow.setDifficultyGrade("D2");
        secondRow.setCurrentSkillTier("A");
        secondRow.setBaselineError(BigDecimal.valueOf(5));
        secondRow.setEquipmentGrade("A");

        when(quantitativeEvaluationQueryMapper.findQuantitativeSourcesForEvaluation(202601L, null, false))
            .thenReturn(List.of(firstRow, secondRow));

        QuantitativeSourceReader reader = new QuantitativeSourceReader(
            quantitativeEvaluationQueryMapper,
            quantitativeEvaluationProcessor,
            evaluationPeriodQueryMapper,
            202601L,
            null,
            "false",
            "MONTH"
        );

        QuantitativeEvaluationAggregate first = reader.read();
        QuantitativeEvaluationAggregate second = reader.read();

        assertThat(first.getSQuant()).isEqualByComparingTo("20.00");
        assertThat(second.getSQuant()).isEqualByComparingTo("60.00");
        assertThat(first.getGroupMean()).isEqualByComparingTo("40.00");
        assertThat(second.getGroupMean()).isEqualByComparingTo("40.00");
        assertThat(first.getGroupStdDev()).isEqualByComparingTo("20.00");
        assertThat(second.getGroupStdDev()).isEqualByComparingTo("20.00");
    }
}
