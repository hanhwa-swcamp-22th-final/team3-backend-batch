package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.api.command.dto.ManualJobLaunchMode;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeEvaluationSubmittedListener {
    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationSubmittedListener.class);
    private static final String DEFAULT_ANALYSIS_STATUS = "SUBMITTED";

    private final BatchJobLauncherFacade batchJobLauncherFacade;
    private final ObjectMapper objectMapper;
    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

    @KafkaListener(
        topics = QualitativeKafkaTopics.QUALITATIVE_EVALUATION_SUBMITTED,
        containerFactory = "qualitativeSubmittedKafkaListenerContainerFactory"
    )
    public void listen(QualitativeEvaluationSubmittedEvent event) {
        upsertProjection(event);

        log.info(
            "Received qualitative submitted event. evaluationId={}, evaluateeId={}, periodId={}, level={}, keywordRuleCount={}",
            event.getQualitativeEvaluationId(),
            event.getEvaluateeId(),
            event.getEvaluationPeriodId(),
            event.getEvaluationLevel(),
            event.getKeywordRules() == null ? 0 : event.getKeywordRules().size()
        );

        batchJobLauncherFacade.launch(
            BatchJobNames.QUALITATIVE_ANALYSIS_JOB,
            BatchJobLaunchRequest.builder()
                .mode(ManualJobLaunchMode.EMPLOYEE)
                .periodType(BatchPeriodType.MONTH)
                .evaluationPeriodId(event.getEvaluationPeriodId())
                .qualitativeEvaluationId(event.getQualitativeEvaluationId())
                .qualitativeEventPayload(toPayload(event))
                .force(Boolean.TRUE)
                .requestedBy("hr-kafka")
                .reason("Qualitative evaluation submitted event")
                .build()
        );
    }

    private void upsertProjection(QualitativeEvaluationSubmittedEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime submittedAt = event.getOccurredAt() == null ? now : event.getOccurredAt();
        String analysisStatus = normalizeAnalysisStatus(event.getStatus());

        QualitativeScoreProjectionEntity projection = qualitativeScoreProjectionRepository.findById(event.getQualitativeEvaluationId())
            .map(existing -> {
                existing.refreshSubmittedSnapshot(
                    event.getEvaluationPeriodId(),
                    event.getEvaluateeId(),
                    event.getEvaluationLevel(),
                    event.getAlgorithmVersionId(),
                    event.getAnalysisVersion(),
                    analysisStatus,
                    submittedAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> QualitativeScoreProjectionEntity.create(
                event.getQualitativeEvaluationId(),
                event.getEvaluationPeriodId(),
                event.getEvaluateeId(),
                event.getEvaluationLevel(),
                event.getAlgorithmVersionId(),
                event.getAnalysisVersion(),
                analysisStatus,
                submittedAt,
                now
            ));

        qualitativeScoreProjectionRepository.save(projection);
    }

    private String normalizeAnalysisStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_ANALYSIS_STATUS;
        }
        return status;
    }

    private String toPayload(QualitativeEvaluationSubmittedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize qualitative submitted event payload.", exception);
        }
    }
}