package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.TierConfigSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.PromotionKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.TierConfigProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.TierConfigProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TierConfigSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(TierConfigSnapshotListener.class);

    private final TierConfigProjectionRepository repository;

    @KafkaListener(
        topics = PromotionKafkaTopics.TIER_CONFIG_SNAPSHOT,
        containerFactory = "tierConfigSnapshotKafkaListenerContainerFactory"
    )
    public void listen(TierConfigSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        TierConfigProjectionEntity projection = repository.findById(event.getTierConfigId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getTier(),
                    event.getWeightDistribution(),
                    event.getPromotionPoint(),
                    event.getActive(),
                    event.getDeleted(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> TierConfigProjectionEntity.create(
                event.getTierConfigId(),
                event.getTier(),
                event.getWeightDistribution(),
                event.getPromotionPoint(),
                event.getActive(),
                event.getDeleted(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted tier config projection. tierConfigId={}, tier={}, promotionPoint={}, active={}, deleted={}",
            event.getTierConfigId(),
            event.getTier(),
            event.getPromotionPoint(),
            event.getActive(),
            event.getDeleted()
        );
    }
}
