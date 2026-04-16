package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EquipmentReferenceSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EquipmentBaselineCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QuantitativeEvaluationEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EquipmentReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EquipmentReferenceProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EquipmentReferenceProjectionRepository;
import com.ohgiraffers.team3backendbatch.domain.quantitative.scoring.QuantitativeScoreCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EquipmentReferenceSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EquipmentReferenceSnapshotListener.class);
    private final EquipmentReferenceProjectionRepository repository;
    private final QuantitativeScoreCalculator quantitativeScoreCalculator;
    private final QuantitativeEvaluationEventPublisher quantitativeEvaluationEventPublisher;

    @KafkaListener(
        topics = EquipmentReferenceKafkaTopics.EQUIPMENT_REFERENCE_SNAPSHOT,
        containerFactory = "equipmentReferenceSnapshotKafkaListenerContainerFactory"
    )
    @Transactional
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
        publishCalculatedBaselineIfChanged(event, occurredAt);
        log.info(
            "Upserted equipment reference projection. equipmentId={}, equipmentCode={}, deleted={}",
            event.getEquipmentId(),
            event.getEquipmentCode(),
            event.getDeleted()
        );
    }

    private void publishCalculatedBaselineIfChanged(EquipmentReferenceSnapshotEvent event, LocalDateTime occurredAt) {
        if (event.getEquipmentId() == null || Boolean.TRUE.equals(event.getDeleted())) {
            return;
        }
        if (event.getEquipmentInstallDate() == null
            || event.getEquipmentWarrantyMonth() == null
            || event.getEquipmentDesignLifeMonths() == null) {
            return;
        }

        BigDecimal nAge = quantitativeScoreCalculator.calculateNAge(
            event.getEquipmentInstallDate().toLocalDate(),
            occurredAt.toLocalDate(),
            event.getEquipmentWarrantyMonth(),
            event.getEquipmentDesignLifeMonths()
        );
        Integer equipmentAgeMonths = quantitativeScoreCalculator.calculateEquipmentAgeMonths(
            event.getEquipmentInstallDate().toLocalDate(),
            occurredAt.toLocalDate()
        );
        String currentEquipmentGrade = quantitativeScoreCalculator.resolveCurrentEquipmentGrade(
            event.getEquipmentGrade(),
            nAge
        );
        BigDecimal etaAge = quantitativeScoreCalculator.calculateEtaAge(event.getEquipmentWearCoefficient(), nAge);
        BigDecimal etaMaint = event.getEquipmentEtaMaint() == null
            ? BigDecimal.ONE
            : event.getEquipmentEtaMaint();
        BigDecimal currentEquipmentIdx = quantitativeScoreCalculator.calculateEIdx(
            currentEquipmentGrade,
            nAge,
            etaAge,
            etaMaint
        );

        if (isSameBigDecimal(event.getEquipmentIdx(), currentEquipmentIdx)
            && isSameBigDecimal(event.getEquipmentEtaMaint(), etaMaint)
            && isSameBigDecimal(event.getEquipmentEtaAge(), etaAge)
            && Objects.equals(event.getEquipmentAgeMonths(), equipmentAgeMonths)
            && Objects.equals(event.getCurrentEquipmentGrade(), currentEquipmentGrade)) {
            return;
        }

        quantitativeEvaluationEventPublisher.publishEquipmentBaselineCalculated(
            EquipmentBaselineCalculatedEvent.builder()
                .equipmentId(event.getEquipmentId())
                .equipmentStandardPerformanceRate(event.getEquipmentStandardPerformanceRate())
                .equipmentBaselineErrorRate(event.getEquipmentBaselineErrorRate())
                .equipmentEtaAge(etaAge)
                .equipmentEtaMaint(etaMaint)
                .equipmentAgeMonths(equipmentAgeMonths)
                .equipmentIdx(currentEquipmentIdx)
                .currentEquipmentGrade(currentEquipmentGrade)
                .calculatedAt(occurredAt)
                .build()
        );
    }


    private boolean isSameBigDecimal(BigDecimal previous, BigDecimal current) {
        if (previous == null || current == null) {
            return previous == current;
        }
        return previous.compareTo(current) == 0;
    }
}
