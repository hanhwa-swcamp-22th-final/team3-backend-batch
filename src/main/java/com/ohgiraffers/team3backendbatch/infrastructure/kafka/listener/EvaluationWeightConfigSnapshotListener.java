package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EvaluationWeightConfigSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.PromotionKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.EvaluationWeightConfigProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.EvaluationWeightConfigProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationWeightConfigSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EvaluationWeightConfigSnapshotListener.class);

    private final EvaluationWeightConfigProjectionRepository repository;

    @KafkaListener(
        topics = PromotionKafkaTopics.EVALUATION_WEIGHT_CONFIG_SNAPSHOT,
        containerFactory = "evaluationWeightConfigSnapshotKafkaListenerContainerFactory"
    )
    public void listen(EvaluationWeightConfigSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        EvaluationWeightConfigProjectionEntity projection = repository.findById(event.getEvaluationWeightConfigId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getTierGroup(),
                    event.getCategoryCode(),
                    event.getWeightPercent(),
                    event.getActive(),
                    event.getDeleted(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> EvaluationWeightConfigProjectionEntity.create(
                event.getEvaluationWeightConfigId(),
                event.getTierGroup(),
                event.getCategoryCode(),
                event.getWeightPercent(),
                event.getActive(),
                event.getDeleted(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted evaluation weight config projection. configId={}, tierGroup={}, categoryCode={}, active={}, deleted={}",
            event.getEvaluationWeightConfigId(),
            event.getTierGroup(),
            event.getCategoryCode(),
            event.getActive(),
            event.getDeleted()
        );
    }
}
