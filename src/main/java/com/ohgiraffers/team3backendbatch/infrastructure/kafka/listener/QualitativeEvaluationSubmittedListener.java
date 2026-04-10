package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.api.command.dto.ManualJobLaunchMode;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeSubmittedEventStore;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
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
    private final QualitativeSubmittedEventStore qualitativeSubmittedEventStore;
    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;
    private final EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;

    @KafkaListener(
        topics = QualitativeKafkaTopics.QUALITATIVE_EVALUATION_SUBMITTED,
        containerFactory = "qualitativeSubmittedKafkaListenerContainerFactory"
    )
    public void listen(QualitativeEvaluationSubmittedEvent event) {
        QualitativeEvaluationSubmittedEvent resolvedEvent = resolveAlgorithmVersionId(event);

        upsertProjection(resolvedEvent);

        log.info(
            "Received qualitative submitted event. evaluationId={}, evaluateeId={}, periodId={}, level={}, keywordRuleCount={}",
            resolvedEvent.getQualitativeEvaluationId(),
            resolvedEvent.getEvaluateeId(),
            resolvedEvent.getEvaluationPeriodId(),
            resolvedEvent.getEvaluationLevel(),
            resolvedEvent.getKeywordRules() == null ? 0 : resolvedEvent.getKeywordRules().size()
        );

        qualitativeSubmittedEventStore.put(resolvedEvent);

        batchJobLauncherFacade.launch(
            BatchJobNames.QUALITATIVE_ANALYSIS_JOB,
            BatchJobLaunchRequest.builder()
                .mode(ManualJobLaunchMode.EMPLOYEE)
                .periodType(BatchPeriodType.MONTH)
                .evaluationPeriodId(resolvedEvent.getEvaluationPeriodId())
                .qualitativeEvaluationId(resolvedEvent.getQualitativeEvaluationId())
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

    private QualitativeEvaluationSubmittedEvent resolveAlgorithmVersionId(QualitativeEvaluationSubmittedEvent event) {
        if (event.getAlgorithmVersionId() != null || event.getEvaluationPeriodId() == null) {
            return event;
        }

        Long resolvedAlgorithmVersionId = evaluationPeriodProjectionRepository.findById(event.getEvaluationPeriodId())
            .map(projection -> projection.getAlgorithmVersionId())
            .orElse(null);

        if (resolvedAlgorithmVersionId == null) {
            log.warn(
                "Qualitative submitted event has no algorithmVersionId and evaluation period projection has none. evaluationId={}, periodId={}",
                event.getQualitativeEvaluationId(),
                event.getEvaluationPeriodId()
            );
            return event;
        }

        event.setAlgorithmVersionId(resolvedAlgorithmVersionId);
        log.info(
            "Resolved algorithmVersionId from evaluation period projection. evaluationId={}, periodId={}, algorithmVersionId={}",
            event.getQualitativeEvaluationId(),
            event.getEvaluationPeriodId(),
            resolvedAlgorithmVersionId
        );
        return event;
    }

    private String normalizeAnalysisStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_ANALYSIS_STATUS;
        }
        return status;
    }

}
