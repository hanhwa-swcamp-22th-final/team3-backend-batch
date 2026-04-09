package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeAnalysisEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QualitativeAnalysisWriterTest {

    @Mock
    private QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

    @Mock
    private QualitativeAnalysisEventPublisher qualitativeAnalysisEventPublisher;

    @Test
    void write_shouldUpdateProjectionAndPublishEvent() throws Exception {
        LocalDateTime analyzedAt = LocalDateTime.of(2026, 4, 6, 10, 30);
        QualitativeAnalysisResult result = QualitativeAnalysisResult.builder()
            .evaluationId(10L)
            .evaluatorId(9001L)
            .algorithmVersionId(1001L)
            .squalRaw(BigDecimal.valueOf(0.8))
            .analysisStatus("COMPLETED")
            .analyzedAt(analyzedAt)
            .build();

        Constructor<QualitativeScoreProjectionEntity> constructor = QualitativeScoreProjectionEntity.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QualitativeScoreProjectionEntity projection = constructor.newInstance();
        ReflectionTestUtils.setField(projection, "qualitativeEvaluationId", 10L);

        when(qualitativeScoreProjectionRepository.findAllByQualitativeEvaluationIdIn(List.of(10L)))
            .thenReturn(List.of(projection));

        QualitativeAnalysisWriter writer = new QualitativeAnalysisWriter(
            qualitativeAnalysisEventPublisher,
            qualitativeScoreProjectionRepository
        );
        writer.write(new Chunk<>(List.of(result)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QualitativeScoreProjectionEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(qualitativeScoreProjectionRepository).saveAll(captor.capture());
        QualitativeScoreProjectionEntity savedProjection = captor.getValue().get(0);
        assertThat(savedProjection.getRawScore()).isEqualByComparingTo("0.8");
        assertThat(savedProjection.getAnalysisStatus()).isEqualTo("COMPLETED");
        assertThat(savedProjection.getAnalyzedAt()).isEqualTo(analyzedAt);
        verify(qualitativeAnalysisEventPublisher).publishAnalyzed(any());
    }

    @Test
    void write_shouldFailWhenTargetProjectionIsMissing() {
        QualitativeAnalysisResult result = QualitativeAnalysisResult.builder()
            .evaluationId(99L)
            .algorithmVersionId(1001L)
            .squalRaw(BigDecimal.valueOf(0.1))
            .analysisStatus("COMPLETED")
            .analyzedAt(LocalDateTime.now())
            .build();

        when(qualitativeScoreProjectionRepository.findAllByQualitativeEvaluationIdIn(List.of(99L)))
            .thenReturn(List.of());

        QualitativeAnalysisWriter writer = new QualitativeAnalysisWriter(
            qualitativeAnalysisEventPublisher,
            qualitativeScoreProjectionRepository
        );

        assertThatThrownBy(() -> writer.write(new Chunk<>(List.of(result))))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
        verify(qualitativeAnalysisEventPublisher, never()).publishAnalyzed(any());
    }
}
