package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderDifficultySnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.OrderDifficultyKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity.OrderDifficultyProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.repository.OrderDifficultyProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDifficultySnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(OrderDifficultySnapshotListener.class);

    private final OrderDifficultyProjectionRepository repository;

    @KafkaListener(
        topics = OrderDifficultyKafkaTopics.ORDER_DIFFICULTY_SNAPSHOT,
        containerFactory = "orderDifficultySnapshotKafkaListenerContainerFactory"
    )
    public void listen(OrderDifficultySnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();
        LocalDateTime analyzedAt = event.getAnalyzedAt() == null ? occurredAt : event.getAnalyzedAt();

        OrderDifficultyProjectionEntity projection = repository.findById(event.getOrderId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getDifficultyScore(),
                    event.getDifficultyGrade(),
                    event.getOrderStatus(),
                    analyzedAt,
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> OrderDifficultyProjectionEntity.create(
                event.getOrderId(),
                event.getDifficultyScore(),
                event.getDifficultyGrade(),
                event.getOrderStatus(),
                analyzedAt,
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted order difficulty projection. orderId={}, difficultyGrade={}, difficultyScore={}, orderStatus={}",
            event.getOrderId(),
            event.getDifficultyGrade(),
            event.getDifficultyScore(),
            event.getOrderStatus()
        );
    }
}
