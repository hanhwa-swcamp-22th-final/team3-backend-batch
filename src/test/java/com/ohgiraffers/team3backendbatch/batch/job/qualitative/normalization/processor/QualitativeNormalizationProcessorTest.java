package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationStatistics;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeEvaluationQueryMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QualitativeNormalizationProcessorTest {

    @Mock
    private QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper;

    @Test
    void process_shouldNormalizeRawScoreUsingPeriodStatistics() throws Exception {
        when(qualitativeEvaluationQueryMapper.findQualitativeNormalizationStatistics(202604L))
            .thenReturn(new QualitativeNormalizationStatistics(
                3L,
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(0.2)
            ));

        QualitativeNormalizationProcessor processor = new QualitativeNormalizationProcessor(
            qualitativeEvaluationQueryMapper,
            new QualitativeScoreCalculator(),
            202604L
        );

        QualitativeNormalizationResult first = processor.process(
            new QualitativeNormalizationTarget(10L, BigDecimal.valueOf(1.2))
        );
        QualitativeNormalizationResult second = processor.process(
            new QualitativeNormalizationTarget(11L, BigDecimal.valueOf(0.8))
        );

        assertThat(first.getEvaluationId()).isEqualTo(10L);
        assertThat(first.getRawScore()).isEqualByComparingTo("1.2");
        assertThat(first.getSQual()).isEqualByComparingTo("60.00");
        assertThat(first.getGrade()).isEqualTo("B");

        assertThat(second.getEvaluationId()).isEqualTo(11L);
        assertThat(second.getSQual()).isEqualByComparingTo("40.00");
        assertThat(second.getGrade()).isEqualTo("C");

        verify(qualitativeEvaluationQueryMapper, times(1)).findQualitativeNormalizationStatistics(202604L);
    }

    @Test
    void process_shouldSkipWhenSampleCountIsInsufficient() throws Exception {
        when(qualitativeEvaluationQueryMapper.findQualitativeNormalizationStatistics(202604L))
            .thenReturn(new QualitativeNormalizationStatistics(
                1L,
                BigDecimal.valueOf(1.0),
                BigDecimal.ZERO
            ));

        QualitativeNormalizationProcessor processor = new QualitativeNormalizationProcessor(
            qualitativeEvaluationQueryMapper,
            new QualitativeScoreCalculator(),
            202604L
        );

        QualitativeNormalizationResult result = processor.process(
            new QualitativeNormalizationTarget(10L, BigDecimal.valueOf(1.2))
        );

        assertThat(result).isNull();
    }
}