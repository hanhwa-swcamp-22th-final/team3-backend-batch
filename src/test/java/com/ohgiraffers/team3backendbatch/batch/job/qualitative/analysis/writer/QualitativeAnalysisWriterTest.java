package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeAnalysisEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.entity.BiasCorrectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.repository.BiasCorrectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeEvaluationRepository;
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
    private QualitativeEvaluationRepository qualitativeEvaluationRepository;

    @Mock
    private BiasCorrectionRepository biasCorrectionRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private QualitativeAnalysisEventPublisher qualitativeAnalysisEventPublisher;

    @Test
    void write_shouldUpdateRawScoreOnlyAndSaveEntity() throws Exception {
        QualitativeAnalysisResult result = QualitativeAnalysisResult.builder()
            .evaluationId(10L)
            .evaluatorId(9001L)
            .squalRaw(BigDecimal.valueOf(0.8))
            .originalSQual(null)
            .sQual(null)
            .normalizedTier(null)
            .matchedKeywordCount(2)
            .matchedKeywords(List.of("maintenance"))
            .contextWeight(BigDecimal.ONE)
            .negationDetected(false)
            .algorithmVersion("squal-v1")
            .analysisStatus("COMPLETED")
            .analyzedAt(LocalDateTime.now())
            .biasCorrected(false)
            .build();

        Constructor<QualitativeEvaluationEntity> constructor = QualitativeEvaluationEntity.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QualitativeEvaluationEntity entity = constructor.newInstance();
        ReflectionTestUtils.setField(entity, "qualitativeEvaluationId", 10L);

        when(qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(List.of(10L)))
            .thenReturn(List.of(entity));

        QualitativeAnalysisWriter writer = new QualitativeAnalysisWriter(
            qualitativeEvaluationRepository,
            biasCorrectionRepository,
            idGenerator,
            qualitativeAnalysisEventPublisher
        );
        writer.write(new Chunk<>(List.of(result)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QualitativeEvaluationEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(qualitativeEvaluationRepository).saveAll(captor.capture());
        QualitativeEvaluationEntity savedEntity = captor.getValue().get(0);
        assertThat(savedEntity.getScore()).isEqualByComparingTo("0.8");
        assertThat(savedEntity.getSQual()).isNull();
        assertThat(savedEntity.getGrade()).isNull();
        verify(biasCorrectionRepository, never()).saveAll(any());
        verify(qualitativeAnalysisEventPublisher).publishAnalyzed(any());
    }

    @Test
    void write_shouldPersistBiasCorrectionWhenCorrectedScoreExists() throws Exception {
        LocalDateTime analyzedAt = LocalDateTime.of(2026, 4, 6, 10, 30);
        QualitativeAnalysisResult result = QualitativeAnalysisResult.builder()
            .evaluationId(11L)
            .evaluatorId(9100L)
            .squalRaw(BigDecimal.valueOf(0.5))
            .originalSQual(BigDecimal.valueOf(84.0))
            .sQual(BigDecimal.valueOf(79.0))
            .normalizedTier("A")
            .analysisStatus("COMPLETED")
            .analyzedAt(analyzedAt)
            .biasCorrected(true)
            .biasType("GENEROUS")
            .evaluatorAverage(BigDecimal.valueOf(90.0))
            .companyAverage(BigDecimal.valueOf(82.0))
            .alphaBias(BigDecimal.valueOf(0.35))
            .build();

        Constructor<QualitativeEvaluationEntity> constructor = QualitativeEvaluationEntity.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        QualitativeEvaluationEntity entity = constructor.newInstance();
        ReflectionTestUtils.setField(entity, "qualitativeEvaluationId", 11L);

        when(qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(List.of(11L)))
            .thenReturn(List.of(entity));
        when(idGenerator.generate()).thenReturn(123456789L);

        QualitativeAnalysisWriter writer = new QualitativeAnalysisWriter(
            qualitativeEvaluationRepository,
            biasCorrectionRepository,
            idGenerator,
            qualitativeAnalysisEventPublisher
        );
        writer.write(new Chunk<>(List.of(result)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BiasCorrectionEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(biasCorrectionRepository).saveAll(captor.capture());
        BiasCorrectionEntity biasEntity = captor.getValue().get(0);
        assertThat(biasEntity.getBiasCorrectionId()).isEqualTo(123456789L);
        assertThat(biasEntity.getEvaluatorId()).isEqualTo(9100L);
        assertThat(biasEntity.getQualitativeEvaluationId()).isEqualTo(11L);
        assertThat(biasEntity.getOriginalScore()).isEqualByComparingTo("84.0");
        assertThat(biasEntity.getCorrectedScore()).isEqualByComparingTo("79.0");
        assertThat(biasEntity.getDetectedAt()).isEqualTo(analyzedAt);
    }

    @Test
    void write_shouldFailWhenTargetEntityIsMissing() {
        QualitativeAnalysisResult result = QualitativeAnalysisResult.builder()
            .evaluationId(99L)
            .evaluatorId(9001L)
            .squalRaw(BigDecimal.valueOf(0.1))
            .originalSQual(null)
            .sQual(null)
            .normalizedTier(null)
            .analysisStatus("COMPLETED")
            .analyzedAt(LocalDateTime.now())
            .biasCorrected(false)
            .build();

        when(qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(List.of(99L)))
            .thenReturn(List.of());

        QualitativeAnalysisWriter writer = new QualitativeAnalysisWriter(
            qualitativeEvaluationRepository,
            biasCorrectionRepository,
            idGenerator,
            qualitativeAnalysisEventPublisher
        );

        assertThatThrownBy(() -> writer.write(new Chunk<>(List.of(result))))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
    }
}