package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PromotionHistorySnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.PromotionKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.PromotionHistoryProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.PromotionHistoryProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionHistorySnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(PromotionHistorySnapshotListener.class);

    private final PromotionHistoryProjectionRepository repository;

    @KafkaListener(
        topics = PromotionKafkaTopics.PROMOTION_HISTORY_SNAPSHOT,
        containerFactory = "promotionHistorySnapshotKafkaListenerContainerFactory"
    )
    public void listen(PromotionHistorySnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        PromotionHistoryProjectionEntity projection = repository.findById(event.getTierPromotionId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEmployeeId(),
                    event.getReviewerId(),
                    event.getCurrentTierConfigId(),
                    event.getTargetTierConfigId(),
                    event.getTierConfigEffectiveDate(),
                    event.getTierAccumulatedPoint(),
                    event.getPromotionStatus(),
                    event.getTierReviewedAt(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> PromotionHistoryProjectionEntity.create(
                event.getTierPromotionId(),
                event.getEmployeeId(),
                event.getReviewerId(),
                event.getCurrentTierConfigId(),
                event.getTargetTierConfigId(),
                event.getTierConfigEffectiveDate(),
                event.getTierAccumulatedPoint(),
                event.getPromotionStatus(),
                event.getTierReviewedAt(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted promotion history projection. promotionId={}, employeeId={}, status={}",
            event.getTierPromotionId(),
            event.getEmployeeId(),
            event.getPromotionStatus()
        );
    }
}
