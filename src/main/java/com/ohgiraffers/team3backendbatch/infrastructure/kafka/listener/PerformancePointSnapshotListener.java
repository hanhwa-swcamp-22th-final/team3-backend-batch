package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.PromotionKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.PerformancePointProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.PerformancePointProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformancePointSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(PerformancePointSnapshotListener.class);

    private final PerformancePointProjectionRepository repository;

    @KafkaListener(
        topics = PromotionKafkaTopics.PERFORMANCE_POINT_SNAPSHOT,
        containerFactory = "performancePointSnapshotKafkaListenerContainerFactory"
    )
    public void listen(PerformancePointSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        PerformancePointProjectionEntity projection = repository.findById(event.getPerformancePointId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEmployeeId(),
                    event.getPointType(),
                    event.getPointAmount(),
                    event.getPointEarnedDate(),
                    event.getPointSourceId(),
                    event.getPointSourceType(),
                    event.getPointDescription(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> PerformancePointProjectionEntity.create(
                event.getPerformancePointId(),
                event.getEmployeeId(),
                event.getPointType(),
                event.getPointAmount(),
                event.getPointEarnedDate(),
                event.getPointSourceId(),
                event.getPointSourceType(),
                event.getPointDescription(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted performance point projection. pointId={}, employeeId={}, pointType={}, pointAmount={}",
            event.getPerformancePointId(),
            event.getEmployeeId(),
            event.getPointType(),
            event.getPointAmount()
        );
    }
}
