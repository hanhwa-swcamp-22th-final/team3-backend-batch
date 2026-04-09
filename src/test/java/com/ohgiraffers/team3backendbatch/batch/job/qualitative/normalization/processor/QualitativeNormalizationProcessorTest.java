package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QualitativeNormalizationProcessorTest {

    @Mock
    private QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

    @Mock
    private QualitativeScoreProjectionRepository.QualitativeScoreProjectionStatisticsView statisticsView;

    @Test
    void process_shouldNormalizeRawScoreUsingPeriodStatistics() throws Exception {
        when(statisticsView.getSampleCount()).thenReturn(3L);
        when(statisticsView.getMeanScore()).thenReturn(BigDecimal.valueOf(1.0));
        when(statisticsView.getStddevScore()).thenReturn(BigDecimal.valueOf(0.2));
        when(qualitativeScoreProjectionRepository.findNormalizationStatistics(202604L))
            .thenReturn(statisticsView);

        QualitativeNormalizationProcessor processor = new QualitativeNormalizationProcessor(
            qualitativeScoreProjectionRepository,
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

        verify(qualitativeScoreProjectionRepository, times(1)).findNormalizationStatistics(202604L);
    }

    @Test
    void process_shouldSkipWhenSampleCountIsInsufficient() throws Exception {
        when(statisticsView.getSampleCount()).thenReturn(1L);
        when(statisticsView.getMeanScore()).thenReturn(BigDecimal.valueOf(1.0));
        when(statisticsView.getStddevScore()).thenReturn(BigDecimal.ZERO);
        when(qualitativeScoreProjectionRepository.findNormalizationStatistics(202604L))
            .thenReturn(statisticsView);

        QualitativeNormalizationProcessor processor = new QualitativeNormalizationProcessor(
            qualitativeScoreProjectionRepository,
            new QualitativeScoreCalculator(),
            202604L
        );

        QualitativeNormalizationResult result = processor.process(
            new QualitativeNormalizationTarget(10L, BigDecimal.valueOf(1.2))
        );

        assertThat(result).isNull();
    }
}
