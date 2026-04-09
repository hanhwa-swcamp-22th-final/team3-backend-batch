package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EnvironmentEventSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EnvironmentReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EnvironmentEventProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EnvironmentEventProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnvironmentEventSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentEventSnapshotListener.class);

    private final EnvironmentEventProjectionRepository repository;

    @KafkaListener(
        topics = EnvironmentReferenceKafkaTopics.ENVIRONMENT_EVENT_SNAPSHOT,
        containerFactory = "environmentEventSnapshotKafkaListenerContainerFactory"
    )
    public void listen(EnvironmentEventSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        EnvironmentEventProjectionEntity projection = repository.findById(event.getEnvironmentEventId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEquipmentId(),
                    event.getEnvTemperature(),
                    event.getEnvHumidity(),
                    event.getEnvParticleCnt(),
                    event.getEnvDeviationType(),
                    event.getEnvCorrectionApplied(),
                    event.getEnvDetectedAt(),
                    Boolean.TRUE.equals(event.getDeleted()),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> EnvironmentEventProjectionEntity.create(
                event.getEnvironmentEventId(),
                event.getEquipmentId(),
                event.getEnvTemperature(),
                event.getEnvHumidity(),
                event.getEnvParticleCnt(),
                event.getEnvDeviationType(),
                event.getEnvCorrectionApplied(),
                event.getEnvDetectedAt(),
                Boolean.TRUE.equals(event.getDeleted()),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted environment event projection. environmentEventId={}, equipmentId={}, deleted={}",
            event.getEnvironmentEventId(),
            event.getEquipmentId(),
            event.getDeleted()
        );
    }
}
