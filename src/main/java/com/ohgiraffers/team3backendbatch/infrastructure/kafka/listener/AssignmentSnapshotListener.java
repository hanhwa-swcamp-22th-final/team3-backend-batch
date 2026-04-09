package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.AssignmentSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.OrderDifficultyKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity.OrderAssignmentProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.repository.OrderAssignmentProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(AssignmentSnapshotListener.class);

    private final OrderAssignmentProjectionRepository repository;

    @KafkaListener(
        topics = OrderDifficultyKafkaTopics.ASSIGNMENT_SNAPSHOT,
        containerFactory = "assignmentSnapshotKafkaListenerContainerFactory"
    )
    public void listen(AssignmentSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();
        LocalDateTime assignedAt = event.getAssignedAt() == null ? occurredAt : event.getAssignedAt();

        OrderAssignmentProjectionEntity projection = repository.findById(event.getMatchingRecordId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getOrderId(),
                    event.getEmployeeId(),
                    event.getMatchingStatus(),
                    assignedAt,
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> OrderAssignmentProjectionEntity.create(
                event.getMatchingRecordId(),
                event.getOrderId(),
                event.getEmployeeId(),
                event.getMatchingStatus(),
                assignedAt,
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted order assignment projection. matchingRecordId={}, orderId={}, employeeId={}, matchingStatus={}",
            event.getMatchingRecordId(),
            event.getOrderId(),
            event.getEmployeeId(),
            event.getMatchingStatus()
        );
    }
}
