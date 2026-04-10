package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeNormalizationEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QualitativeNormalizationWriterTest {

    @Mock
    private QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

    @Mock
    private QualitativeNormalizationEventPublisher qualitativeNormalizationEventPublisher;

    @Test
    void write_shouldUpdateNormalizedScoreAndGrade() throws Exception {
        QualitativeNormalizationResult result = QualitativeNormalizationResult.builder()
            .evaluationId(10L)
            .rawScore(BigDecimal.valueOf(1.3200))
            .sQual(BigDecimal.valueOf(73.50))
            .grade("A")
            .build();

        Constructor<QualitativeScoreProjectionEntity> constructor = QualitativeScoreProjectionEntity.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QualitativeScoreProjectionEntity projection = constructor.newInstance();
        ReflectionTestUtils.setField(projection, "qualitativeEvaluationId", 10L);
        ReflectionTestUtils.setField(projection, "rawScore", BigDecimal.valueOf(1.3200));

        when(qualitativeScoreProjectionRepository.findAllByQualitativeEvaluationIdIn(List.of(10L)))
            .thenReturn(List.of(projection));

        QualitativeNormalizationWriter writer = new QualitativeNormalizationWriter(
            qualitativeScoreProjectionRepository,
            qualitativeNormalizationEventPublisher
        );
        writer.write(new Chunk<>(List.of(result)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QualitativeScoreProjectionEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(qualitativeScoreProjectionRepository).saveAll(captor.capture());
        QualitativeScoreProjectionEntity savedProjection = captor.getValue().get(0);
        assertThat(savedProjection.getRawScore()).isEqualByComparingTo("1.32");
        assertThat(savedProjection.getNormalizedScore()).isEqualByComparingTo("73.5");
        assertThat(savedProjection.getGrade()).isEqualTo("A");
        verify(qualitativeNormalizationEventPublisher).publishNormalized(any());
    }

    @Test
    void write_shouldFailWhenTargetEntityIsMissing() {
        QualitativeNormalizationResult result = QualitativeNormalizationResult.builder()
            .evaluationId(99L)
            .rawScore(BigDecimal.valueOf(0.5))
            .sQual(BigDecimal.valueOf(60.0))
            .grade("B")
            .build();

        when(qualitativeScoreProjectionRepository.findAllByQualitativeEvaluationIdIn(List.of(99L)))
            .thenReturn(List.of());

        QualitativeNormalizationWriter writer = new QualitativeNormalizationWriter(
            qualitativeScoreProjectionRepository,
            qualitativeNormalizationEventPublisher
        );

        assertThatThrownBy(() -> writer.write(new Chunk<>(List.of(result))))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
        verify(qualitativeNormalizationEventPublisher, never()).publishNormalized(any());
    }
}
