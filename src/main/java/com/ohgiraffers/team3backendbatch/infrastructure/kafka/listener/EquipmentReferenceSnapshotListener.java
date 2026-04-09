package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EquipmentReferenceSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EquipmentReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EquipmentReferenceProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EquipmentReferenceProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EquipmentReferenceSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EquipmentReferenceSnapshotListener.class);

    private final EquipmentReferenceProjectionRepository repository;

    @KafkaListener(
        topics = EquipmentReferenceKafkaTopics.EQUIPMENT_REFERENCE_SNAPSHOT,
        containerFactory = "equipmentReferenceSnapshotKafkaListenerContainerFactory"
    )
    public void listen(EquipmentReferenceSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        EquipmentReferenceProjectionEntity projection = repository.findById(event.getEquipmentId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEquipmentCode(),
                    event.getEquipmentStatus(),
                    event.getEquipmentGrade(),
                    event.getEquipmentInstallDate(),
                    event.getEnvironmentStandardId(),
                    event.getEquipmentWarrantyMonth(),
                    event.getEquipmentDesignLifeMonths(),
                    event.getEquipmentWearCoefficient(),
                    event.getEquipmentStandardPerformanceRate(),
                    event.getEquipmentBaselineErrorRate(),
                    event.getEquipmentEtaMaint(),
                    event.getEquipmentIdx(),
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
            .orElseGet(() -> EquipmentReferenceProjectionEntity.create(
                event.getEquipmentId(),
                event.getEquipmentCode(),
                event.getEquipmentStatus(),
                event.getEquipmentGrade(),
                event.getEquipmentInstallDate(),
                event.getEnvironmentStandardId(),
                event.getEquipmentWarrantyMonth(),
                event.getEquipmentDesignLifeMonths(),
                event.getEquipmentWearCoefficient(),
                event.getEquipmentStandardPerformanceRate(),
                event.getEquipmentBaselineErrorRate(),
                event.getEquipmentEtaMaint(),
                event.getEquipmentIdx(),
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
            "Upserted equipment reference projection. equipmentId={}, equipmentCode={}, deleted={}",
            event.getEquipmentId(),
            event.getEquipmentCode(),
            event.getDeleted()
        );
    }
}
