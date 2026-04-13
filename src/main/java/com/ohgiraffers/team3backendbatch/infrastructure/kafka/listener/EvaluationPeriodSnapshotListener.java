package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EvaluationPeriodSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EvaluationReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationPeriodSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EvaluationPeriodSnapshotListener.class);

    private final EvaluationPeriodProjectionRepository repository;

    @KafkaListener(
        topics = EvaluationReferenceKafkaTopics.EVALUATION_PERIOD_SNAPSHOT,
        containerFactory = "evaluationPeriodSnapshotKafkaListenerContainerFactory"
    )
    public void listen(EvaluationPeriodSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        EvaluationPeriodProjectionEntity projection = repository.findById(event.getEvaluationPeriodId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getAlgorithmVersionId(),
                    event.getEvaluationYear(),
                    event.getEvaluationSequence(),
                    event.getEvaluationType(),
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getStatus(),
                    event.getAlgorithmVersionNo(),
                    event.getAlgorithmImplementationKey(),
                    event.getPolicyConfig(),
                    event.getParameters(),
                    event.getReferenceValues(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> EvaluationPeriodProjectionEntity.create(
                event.getEvaluationPeriodId(),
                event.getAlgorithmVersionId(),
                event.getEvaluationYear(),
                event.getEvaluationSequence(),
                event.getEvaluationType(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus(),
                event.getAlgorithmVersionNo(),
                event.getAlgorithmImplementationKey(),
                event.getPolicyConfig(),
                event.getParameters(),
                event.getReferenceValues(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted evaluation period projection. periodId={}, algorithmVersionId={}, status={}",
            event.getEvaluationPeriodId(),
            event.getAlgorithmVersionId(),
            event.getStatus()
        );
    }
}
