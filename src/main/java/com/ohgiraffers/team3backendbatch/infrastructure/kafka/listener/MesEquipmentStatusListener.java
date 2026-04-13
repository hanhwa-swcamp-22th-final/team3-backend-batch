package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesEquipmentStatusEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MesKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesEquipmentStatusProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesEquipmentStatusProjectionId;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.MesEquipmentStatusProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MesEquipmentStatusListener {

    private static final Logger log = LoggerFactory.getLogger(MesEquipmentStatusListener.class);

    private final MesEquipmentStatusProjectionRepository repository;

    @KafkaListener(
        topics = MesKafkaTopics.MES_EQUIPMENT_STATUS,
        containerFactory = "mesEquipmentStatusKafkaListenerContainerFactory"
    )
    public void listen(MesEquipmentStatusEvent event) {
        if (event == null) {
            log.warn("Skipped malformed MES equipment status event.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();
        MesEquipmentStatusProjectionId projectionId = new MesEquipmentStatusProjectionId(
            event.getEquipmentId(),
            event.getStatusType(),
            event.getStartTimeStamp()
        );

        MesEquipmentStatusProjectionEntity projection = repository.findById(projectionId)
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getSourceEquipmentCode(),
                    event.getEndTimeStamp(),
                    event.getAlarmCode(),
                    event.getAlarmDesc(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> MesEquipmentStatusProjectionEntity.create(
                event.getEquipmentId(),
                event.getStatusType(),
                event.getStartTimeStamp(),
                event.getSourceEquipmentCode(),
                event.getEndTimeStamp(),
                event.getAlarmCode(),
                event.getAlarmDesc(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info("Upserted MES equipment status projection. equipmentId={}, statusType={}, startTimeStamp={}",
            event.getEquipmentId(), event.getStatusType(), event.getStartTimeStamp());
    }
}
