package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesQualityMeasurementEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MesKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesQualityMeasurementProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesQualityMeasurementProjectionId;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.MesQualityMeasurementProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MesQualityMeasurementListener {

    private static final Logger log = LoggerFactory.getLogger(MesQualityMeasurementListener.class);

    private final MesQualityMeasurementProjectionRepository repository;

    @KafkaListener(
        topics = MesKafkaTopics.MES_QUALITY_MEASUREMENT,
        containerFactory = "mesQualityMeasurementKafkaListenerContainerFactory"
    )
    public void listen(MesQualityMeasurementEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();
        MesQualityMeasurementProjectionId projectionId = new MesQualityMeasurementProjectionId(
            event.getQualityResultId(),
            event.getProcessCode(),
            event.getMeasureItem()
        );

        MesQualityMeasurementProjectionEntity projection = repository.findById(projectionId)
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getProdLotNo(),
                    event.getInputLotNo(),
                    event.getUcl(),
                    event.getTargetValue(),
                    event.getLcl(),
                    event.getMeasuredValue(),
                    event.getJudgeResult(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> MesQualityMeasurementProjectionEntity.create(
                event.getQualityResultId(),
                event.getProcessCode(),
                event.getMeasureItem(),
                event.getProdLotNo(),
                event.getInputLotNo(),
                event.getUcl(),
                event.getTargetValue(),
                event.getLcl(),
                event.getMeasuredValue(),
                event.getJudgeResult(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info("Upserted MES quality measurement projection. qualityResultId={}, processCode={}, measureItem={}",
            event.getQualityResultId(), event.getProcessCode(), event.getMeasureItem());
    }
}
