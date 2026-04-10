package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesEnvironmentEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MesKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EnvironmentEventProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EnvironmentStandardProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EquipmentReferenceProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EnvironmentEventProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EnvironmentStandardProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EquipmentReferenceProjectionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MesEnvironmentListener {

    private static final Logger log = LoggerFactory.getLogger(MesEnvironmentListener.class);

    private final EnvironmentEventProjectionRepository repository;
    private final EquipmentReferenceProjectionRepository equipmentReferenceRepository;
    private final EnvironmentStandardProjectionRepository environmentStandardRepository;

    @KafkaListener(
        topics = MesKafkaTopics.MES_ENVIRONMENT,
        containerFactory = "mesEnvironmentKafkaListenerContainerFactory"
    )
    public void listen(MesEnvironmentEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();
        EnvironmentThreshold thresholds = resolveThresholds(event.getEquipmentId());
        DerivedEnvironmentStatus derived = derive(event, thresholds);

        EnvironmentEventProjectionEntity projection = repository.findById(event.getEventId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEquipmentId(),
                    thresholds.environmentStandardId(),
                    event.getSourceEquipmentCode(),
                    event.getEnvTemperature(),
                    event.getEnvHumidity(),
                    event.getEnvParticleCnt(),
                    derived.deviationType(),
                    derived.correctionApplied(),
                    event.getEnvDetectedAt(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> EnvironmentEventProjectionEntity.create(
                event.getEventId(),
                event.getEquipmentId(),
                thresholds.environmentStandardId(),
                event.getSourceEquipmentCode(),
                event.getEnvTemperature(),
                event.getEnvHumidity(),
                event.getEnvParticleCnt(),
                derived.deviationType(),
                derived.correctionApplied(),
                event.getEnvDetectedAt(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted environment event projection. eventId={}, equipmentId={}, deviationType={}, correctionApplied={}",
            event.getEventId(),
            event.getEquipmentId(),
            derived.deviationType(),
            derived.correctionApplied()
        );
    }

    private EnvironmentThreshold resolveThresholds(Long equipmentId) {
        EquipmentReferenceProjectionEntity equipmentReference = equipmentReferenceRepository.findById(equipmentId)
            .filter(reference -> !Boolean.TRUE.equals(reference.getDeleted()))
            .orElse(null);

        if (equipmentReference == null) {
            return new EnvironmentThreshold(null, null, null, null, null, null);
        }

        Long environmentStandardId = equipmentReference.getEnvironmentStandardId();
        EnvironmentStandardProjectionEntity standard = environmentStandardId == null ? null
            : environmentStandardRepository.findById(environmentStandardId)
                .filter(found -> !Boolean.TRUE.equals(found.getDeleted()))
                .orElse(null);

        if (standard != null) {
            return new EnvironmentThreshold(
                environmentStandardId,
                standard.getEnvTempMin(),
                standard.getEnvTempMax(),
                standard.getEnvHumidityMin(),
                standard.getEnvHumidityMax(),
                standard.getEnvParticleLimit()
            );
        }

        return new EnvironmentThreshold(
            environmentStandardId,
            equipmentReference.getEnvTempMin(),
            equipmentReference.getEnvTempMax(),
            equipmentReference.getEnvHumidityMin(),
            equipmentReference.getEnvHumidityMax(),
            equipmentReference.getEnvParticleLimit()
        );
    }

    private DerivedEnvironmentStatus derive(MesEnvironmentEvent event, EnvironmentThreshold thresholds) {
        List<String> deviationTypes = new ArrayList<>();

        if (isBelow(event.getEnvTemperature(), thresholds.tempMin())) {
            deviationTypes.add("TEMP_LOW");
        } else if (isAbove(event.getEnvTemperature(), thresholds.tempMax())) {
            deviationTypes.add("TEMP_HIGH");
        }

        if (isBelow(event.getEnvHumidity(), thresholds.humidityMin())) {
            deviationTypes.add("HUMIDITY_LOW");
        } else if (isAbove(event.getEnvHumidity(), thresholds.humidityMax())) {
            deviationTypes.add("HUMIDITY_HIGH");
        }

        if (event.getEnvParticleCnt() != null
            && thresholds.particleLimit() != null
            && event.getEnvParticleCnt() > thresholds.particleLimit()) {
            deviationTypes.add("PARTICLE_HIGH");
        }

        if (deviationTypes.isEmpty()) {
            return new DerivedEnvironmentStatus("NORMAL", false);
        }

        if (deviationTypes.size() > 1) {
            return new DerivedEnvironmentStatus("COMPOSITE", true);
        }

        return new DerivedEnvironmentStatus(deviationTypes.getFirst(), true);
    }

    private boolean isBelow(BigDecimal value, BigDecimal min) {
        return value != null && min != null && value.compareTo(min) < 0;
    }

    private boolean isAbove(BigDecimal value, BigDecimal max) {
        return value != null && max != null && value.compareTo(max) > 0;
    }

    private record EnvironmentThreshold(
        Long environmentStandardId,
        BigDecimal tempMin,
        BigDecimal tempMax,
        BigDecimal humidityMin,
        BigDecimal humidityMax,
        Integer particleLimit
    ) {
    }

    private record DerivedEnvironmentStatus(String deviationType, boolean correctionApplied) {
    }
}
