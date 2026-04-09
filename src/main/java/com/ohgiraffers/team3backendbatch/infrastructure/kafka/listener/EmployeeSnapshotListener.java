package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EmployeeSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EmployeeReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSnapshotListener.class);

    private final EmployeeProjectionRepository repository;

    @KafkaListener(
        topics = EmployeeReferenceKafkaTopics.EMPLOYEE_SNAPSHOT,
        containerFactory = "employeeSnapshotKafkaListenerContainerFactory"
    )
    public void listen(EmployeeSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        EmployeeProjectionEntity projection = repository.findById(event.getEmployeeId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEmployeeCode(),
                    event.getEmployeeTier(),
                    event.getEmployeeStatus(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> EmployeeProjectionEntity.create(
                event.getEmployeeId(),
                event.getEmployeeCode(),
                event.getEmployeeTier(),
                event.getEmployeeStatus(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted employee projection. employeeId={}, employeeCode={}, employeeTier={}, employeeStatus={}",
            event.getEmployeeId(),
            event.getEmployeeCode(),
            event.getEmployeeTier(),
            event.getEmployeeStatus()
        );
    }
}
