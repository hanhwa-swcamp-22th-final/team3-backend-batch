package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDeploymentSnapshotEvent {

    private Long workerDeploymentId;
    private Long employeeId;
    private Long equipmentId;
    private String workerDeploymentRole;
    private LocalDate startDate;
    private LocalDate endDate;
    private String shift;
    private LocalDateTime occurredAt;
}
