package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EnvironmentStandardSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EnvironmentReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EnvironmentStandardProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EnvironmentStandardProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnvironmentStandardSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentStandardSnapshotListener.class);

    private final EnvironmentStandardProjectionRepository repository;

    @KafkaListener(
        topics = EnvironmentReferenceKafkaTopics.ENVIRONMENT_STANDARD_SNAPSHOT,
        containerFactory = "environmentStandardSnapshotKafkaListenerContainerFactory"
    )
    public void listen(EnvironmentStandardSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        EnvironmentStandardProjectionEntity projection = repository.findById(event.getEnvironmentStandardId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEnvironmentType(),
                    event.getEnvironmentCode(),
                    event.getEnvironmentName(),
                    event.getEnvTempMin(),
                    event.getEnvTempMax(),
                    event.getEnvHumidityMin(),
                    event.getEnvHumidityMax(),
                    event.getEnvParticleLimit(),
                    Boolean.TRUE.equals(event.getDeleted()),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> EnvironmentStandardProjectionEntity.create(
                event.getEnvironmentStandardId(),
                event.getEnvironmentType(),
                event.getEnvironmentCode(),
                event.getEnvironmentName(),
                event.getEnvTempMin(),
                event.getEnvTempMax(),
                event.getEnvHumidityMin(),
                event.getEnvHumidityMax(),
                event.getEnvParticleLimit(),
                Boolean.TRUE.equals(event.getDeleted()),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted environment standard projection. environmentStandardId={}, deleted={}",
            event.getEnvironmentStandardId(),
            event.getDeleted()
        );
    }
}
