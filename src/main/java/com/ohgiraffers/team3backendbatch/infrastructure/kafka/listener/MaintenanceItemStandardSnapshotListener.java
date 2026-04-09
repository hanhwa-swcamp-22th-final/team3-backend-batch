package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MaintenanceItemStandardSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MaintenanceReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MaintenanceItemStandardProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.MaintenanceItemStandardProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaintenanceItemStandardSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceItemStandardSnapshotListener.class);

    private final MaintenanceItemStandardProjectionRepository repository;

    @KafkaListener(
        topics = MaintenanceReferenceKafkaTopics.MAINTENANCE_ITEM_STANDARD_SNAPSHOT,
        containerFactory = "maintenanceItemStandardSnapshotKafkaListenerContainerFactory"
    )
    public void listen(MaintenanceItemStandardSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        MaintenanceItemStandardProjectionEntity projection = repository.findById(event.getMaintenanceItemStandardId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getMaintenanceItem(),
                    event.getMaintenanceWeight(),
                    event.getMaintenanceScoreMax(),
                    Boolean.TRUE.equals(event.getDeleted()),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> MaintenanceItemStandardProjectionEntity.create(
                event.getMaintenanceItemStandardId(),
                event.getMaintenanceItem(),
                event.getMaintenanceWeight(),
                event.getMaintenanceScoreMax(),
                Boolean.TRUE.equals(event.getDeleted()),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted maintenance item standard projection. maintenanceItemStandardId={}, deleted={}",
            event.getMaintenanceItemStandardId(),
            event.getDeleted()
        );
    }
}
