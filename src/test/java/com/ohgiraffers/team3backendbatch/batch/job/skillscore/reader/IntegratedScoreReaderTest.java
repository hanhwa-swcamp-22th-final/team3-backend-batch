package com.ohgiraffers.team3backendbatch.batch.job.skillscore.reader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeSkillKeywordClassifier;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.repository.KmsArticleProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.repository.OrderAssignmentProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.EvaluationWeightConfigProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.EvaluationCommentRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.QuantitativeEvaluationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IntegratedScoreReaderTest {

    @Mock
    private EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;

    @Mock
    private EmployeeProjectionRepository employeeProjectionRepository;

    @Mock
    private QuantitativeEvaluationRepository quantitativeEvaluationRepository;

    @Mock
    private QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

    @Mock
    private EvaluationCommentRepository evaluationCommentRepository;

    @Mock
    private OrderAssignmentProjectionRepository orderAssignmentProjectionRepository;

    @Mock
    private KmsArticleProjectionRepository kmsArticleProjectionRepository;

    @Mock
    private EvaluationWeightConfigProjectionRepository evaluationWeightConfigProjectionRepository;

    @Mock
    private QualitativeSkillKeywordClassifier qualitativeSkillKeywordClassifier;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private IntegratedScoreReader integratedScoreReader;

    @Test
    @DisplayName("manual monthly score aggregation rejects non comprehensive or non confirmed period")
    void readRejectsMismatchedRequestedEvaluationPeriod() {
        Long evaluationPeriodId = 1775625914826156L;
        EvaluationPeriodProjectionEntity qualitativeClosingPeriod = EvaluationPeriodProjectionEntity.create(
            evaluationPeriodId,
            1L,
            2026,
            4,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 30),
            "CLOSING",
            "v1",
            "impl",
            null,
            null,
            null,
            LocalDateTime.of(2026, 4, 30, 23, 59),
            LocalDateTime.of(2026, 4, 30, 23, 59)
        );
        when(evaluationPeriodProjectionRepository.findById(evaluationPeriodId)).thenReturn(Optional.of(qualitativeClosingPeriod));
        ReflectionTestUtils.setField(integratedScoreReader, "requestedEvaluationPeriodId", evaluationPeriodId);
        ReflectionTestUtils.setField(integratedScoreReader, "requestedPeriodType", "MONTH");

        assertThatThrownBy(() -> integratedScoreReader.read())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not confirmed");

        verify(evaluationPeriodProjectionRepository).findById(evaluationPeriodId);
        verifyNoInteractions(
            employeeProjectionRepository,
            quantitativeEvaluationRepository,
            qualitativeScoreProjectionRepository,
            evaluationCommentRepository,
            orderAssignmentProjectionRepository,
            kmsArticleProjectionRepository,
            evaluationWeightConfigProjectionRepository,
            qualitativeSkillKeywordClassifier,
            objectMapper
        );
    }
}
