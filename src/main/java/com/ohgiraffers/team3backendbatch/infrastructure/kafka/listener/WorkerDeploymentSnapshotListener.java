package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.WorkerDeploymentSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.DeploymentReferenceKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.WorkerDeploymentProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.WorkerDeploymentProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerDeploymentSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(WorkerDeploymentSnapshotListener.class);

    private final WorkerDeploymentProjectionRepository repository;

    @KafkaListener(
        topics = DeploymentReferenceKafkaTopics.WORKER_DEPLOYMENT_SNAPSHOT,
        containerFactory = "workerDeploymentSnapshotKafkaListenerContainerFactory"
    )
    public void listen(WorkerDeploymentSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        WorkerDeploymentProjectionEntity projection = repository.findById(event.getWorkerDeploymentId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getEmployeeId(),
                    event.getEquipmentId(),
                    event.getWorkerDeploymentRole(),
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getShift(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> WorkerDeploymentProjectionEntity.create(
                event.getWorkerDeploymentId(),
                event.getEmployeeId(),
                event.getEquipmentId(),
                event.getWorkerDeploymentRole(),
                event.getStartDate(),
                event.getEndDate(),
                event.getShift(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted worker deployment projection. deploymentId={}, employeeId={}, equipmentId={}, role={}",
            event.getWorkerDeploymentId(),
            event.getEmployeeId(),
            event.getEquipmentId(),
            event.getWorkerDeploymentRole()
        );
    }
}
