package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeEvaluationRepository;
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
    private QualitativeEvaluationRepository qualitativeEvaluationRepository;

    @Test
    void write_shouldUpdateNormalizedScoreAndGrade() throws Exception {
        QualitativeNormalizationResult result = QualitativeNormalizationResult.builder()
            .evaluationId(10L)
            .rawScore(BigDecimal.valueOf(1.3200))
            .sQual(BigDecimal.valueOf(73.50))
            .grade("A")
            .build();

        Constructor<QualitativeEvaluationEntity> constructor = QualitativeEvaluationEntity.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QualitativeEvaluationEntity entity = constructor.newInstance();
        ReflectionTestUtils.setField(entity, "qualitativeEvaluationId", 10L);
        ReflectionTestUtils.setField(entity, "score", BigDecimal.valueOf(1.3200));

        when(qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(List.of(10L)))
            .thenReturn(List.of(entity));

        QualitativeNormalizationWriter writer = new QualitativeNormalizationWriter(qualitativeEvaluationRepository);
        writer.write(new Chunk<>(List.of(result)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QualitativeEvaluationEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(qualitativeEvaluationRepository).saveAll(captor.capture());
        QualitativeEvaluationEntity savedEntity = captor.getValue().get(0);
        assertThat(savedEntity.getScore()).isEqualByComparingTo("1.32");
        assertThat(savedEntity.getSQual()).isEqualByComparingTo("73.5");
        assertThat(savedEntity.getGrade()).isEqualTo("A");
    }

    @Test
    void write_shouldFailWhenTargetEntityIsMissing() {
        QualitativeNormalizationResult result = QualitativeNormalizationResult.builder()
            .evaluationId(99L)
            .rawScore(BigDecimal.valueOf(0.5))
            .sQual(BigDecimal.valueOf(60.0))
            .grade("B")
            .build();

        when(qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(List.of(99L)))
            .thenReturn(List.of());

        QualitativeNormalizationWriter writer = new QualitativeNormalizationWriter(qualitativeEvaluationRepository);

        assertThatThrownBy(() -> writer.write(new Chunk<>(List.of(result))))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
    }
}