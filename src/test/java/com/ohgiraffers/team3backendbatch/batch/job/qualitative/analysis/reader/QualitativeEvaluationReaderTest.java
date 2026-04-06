package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeEvaluationQueryMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QualitativeEvaluationReaderTest {

    @Mock
    private QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper;

    @Test
    void read_shouldReturnItemsSequentiallyFromMapperResult() {
        QualitativeEvaluationAggregate first = new QualitativeEvaluationAggregate(
            1L,
            202604L,
            101L,
            201L,
            1L,
            null,
            null,
            "first comment",
            "TEXT",
            "squal-v1",
            LocalDateTime.now()
        );
        QualitativeEvaluationAggregate second = new QualitativeEvaluationAggregate(
            2L,
            202604L,
            101L,
            202L,
            2L,
            SecondEvaluationMode.ANALYZE_COMMENT,
            BigDecimal.valueOf(1.3200),
            "second comment",
            "TEXT",
            "squal-v1",
            LocalDateTime.now()
        );

        when(qualitativeEvaluationQueryMapper.findQualitativeEvaluationsForAnalysis(202604L, 101L, null, false, "squal-v1"))
            .thenReturn(List.of(first, second));

        QualitativeEvaluationReader reader =
            new QualitativeEvaluationReader(qualitativeEvaluationQueryMapper, 202604L, 101L, null, "false", "squal-v1");

        assertThat(reader.read()).isEqualTo(first);
        assertThat(reader.read()).isEqualTo(second);
        assertThat(reader.read()).isNull();

        verify(qualitativeEvaluationQueryMapper)
            .findQualitativeEvaluationsForAnalysis(202604L, 101L, null, false, "squal-v1");
        verifyNoMoreInteractions(qualitativeEvaluationQueryMapper);
    }

    @Test
    void read_shouldSupportSingleEvaluationLookup() {
        QualitativeEvaluationAggregate target = new QualitativeEvaluationAggregate(
            11L,
            202604L,
            101L,
            201L,
            2L,
            SecondEvaluationMode.KEEP_FIRST_SCORE,
            BigDecimal.valueOf(1.3200),
            null,
            "TEXT",
            "squal-v1",
            LocalDateTime.now()
        );

        when(qualitativeEvaluationQueryMapper.findQualitativeEvaluationsForAnalysis(202604L, null, 11L, true, "squal-v1"))
            .thenReturn(List.of(target));

        QualitativeEvaluationReader reader =
            new QualitativeEvaluationReader(qualitativeEvaluationQueryMapper, 202604L, null, 11L, "true", "squal-v1");

        assertThat(reader.read()).isEqualTo(target);
        assertThat(reader.read()).isNull();

        verify(qualitativeEvaluationQueryMapper)
            .findQualitativeEvaluationsForAnalysis(202604L, null, 11L, true, "squal-v1");
        verifyNoMoreInteractions(qualitativeEvaluationQueryMapper);
    }
}