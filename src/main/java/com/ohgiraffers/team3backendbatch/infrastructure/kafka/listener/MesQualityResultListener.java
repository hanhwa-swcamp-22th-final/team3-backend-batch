package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesQualityResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MesKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesQualityResultProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.MesQualityResultProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MesQualityResultListener {

    private static final Logger log = LoggerFactory.getLogger(MesQualityResultListener.class);

    private final MesQualityResultProjectionRepository repository;

    @KafkaListener(
        topics = MesKafkaTopics.MES_QUALITY_RESULT,
        containerFactory = "mesQualityResultKafkaListenerContainerFactory"
    )
    public void listen(MesQualityResultEvent event) {
        if (event == null) {
            log.warn("Skipped malformed MES quality result event.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        MesQualityResultProjectionEntity projection = repository.findById(event.getQualityResultId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getProdLotNo(),
                    event.getEquipmentId(),
                    event.getSourceEquipmentCode(),
                    event.getInputLotNo(),
                    event.getEventTimeStamp(),
                    event.getOverallResult(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> MesQualityResultProjectionEntity.create(
                event.getQualityResultId(),
                event.getProdLotNo(),
                event.getEquipmentId(),
                event.getSourceEquipmentCode(),
                event.getInputLotNo(),
                event.getEventTimeStamp(),
                event.getOverallResult(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info("Upserted MES quality result projection. qualityResultId={}, equipmentId={}",
            event.getQualityResultId(), event.getEquipmentId());
    }
}
