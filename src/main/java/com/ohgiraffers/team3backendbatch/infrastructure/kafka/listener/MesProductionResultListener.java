package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesProductionResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MesKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesProductionResultProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.MesProductionResultProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MesProductionResultListener {

    private static final Logger log = LoggerFactory.getLogger(MesProductionResultListener.class);

    private final MesProductionResultProjectionRepository repository;

    @KafkaListener(
        topics = MesKafkaTopics.MES_PRODUCTION_RESULT,
        containerFactory = "mesProductionResultKafkaListenerContainerFactory"
    )
    public void listen(MesProductionResultEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        MesProductionResultProjectionEntity projection = repository.findById(event.getEventId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEquipmentId(),
                    event.getSourceEquipmentCode(),
                    event.getEquipmentNameSnapshot(),
                    event.getInputLotNo(),
                    event.getStartTime(),
                    event.getEndTime(),
                    event.getCycleTimeSec(),
                    event.getInputQty(),
                    event.getOutputQty(),
                    event.getGoodQty(),
                    event.getDefectQty(),
                    event.getLeadTimeSec(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> MesProductionResultProjectionEntity.create(
                event.getEventId(),
                event.getEquipmentId(),
                event.getSourceEquipmentCode(),
                event.getEquipmentNameSnapshot(),
                event.getInputLotNo(),
                event.getStartTime(),
                event.getEndTime(),
                event.getCycleTimeSec(),
                event.getInputQty(),
                event.getOutputQty(),
                event.getGoodQty(),
                event.getDefectQty(),
                event.getLeadTimeSec(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info("Upserted MES production result projection. eventId={}, equipmentId={}", event.getEventId(), event.getEquipmentId());
    }
}
