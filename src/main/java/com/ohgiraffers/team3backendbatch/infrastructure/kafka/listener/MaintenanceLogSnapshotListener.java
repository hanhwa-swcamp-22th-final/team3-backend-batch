package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MaintenanceLogSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MaintenanceReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MaintenanceLogProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.MaintenanceLogProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaintenanceLogSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceLogSnapshotListener.class);

    private final MaintenanceLogProjectionRepository repository;

    @KafkaListener(
        topics = MaintenanceReferenceKafkaTopics.MAINTENANCE_LOG_SNAPSHOT,
        containerFactory = "maintenanceLogSnapshotKafkaListenerContainerFactory"
    )
    public void listen(MaintenanceLogSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        MaintenanceLogProjectionEntity projection = repository.findById(event.getMaintenanceLogId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEquipmentId(),
                    event.getMaintenanceItemStandardId(),
                    event.getMaintenanceType(),
                    event.getMaintenanceDate(),
                    event.getMaintenanceScore(),
                    event.getEtaMaintDelta(),
                    event.getMaintenanceResult(),
                    Boolean.TRUE.equals(event.getDeleted()),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> MaintenanceLogProjectionEntity.create(
                event.getMaintenanceLogId(),
                event.getEquipmentId(),
                event.getMaintenanceItemStandardId(),
                event.getMaintenanceType(),
                event.getMaintenanceDate(),
                event.getMaintenanceScore(),
                event.getEtaMaintDelta(),
                event.getMaintenanceResult(),
                Boolean.TRUE.equals(event.getDeleted()),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted maintenance log projection. maintenanceLogId={}, equipmentId={}, deleted={}",
            event.getMaintenanceLogId(),
            event.getEquipmentId(),
            event.getDeleted()
        );
    }
}
