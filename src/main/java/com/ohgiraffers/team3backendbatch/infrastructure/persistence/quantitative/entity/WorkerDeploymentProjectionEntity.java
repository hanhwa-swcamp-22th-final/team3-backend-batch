package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "worker_deployment_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkerDeploymentProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "worker_deployment_id")
    private Long workerDeploymentId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "worker_deployment_role")
    private String workerDeploymentRole;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "shift")
    private String shift;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static WorkerDeploymentProjectionEntity create(
        Long workerDeploymentId,
        Long employeeId,
        Long equipmentId,
        String workerDeploymentRole,
        LocalDate startDate,
        LocalDate endDate,
        String shift,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        WorkerDeploymentProjectionEntity entity = new WorkerDeploymentProjectionEntity();
        entity.workerDeploymentId = workerDeploymentId;
        entity.employeeId = employeeId;
        entity.equipmentId = equipmentId;
        entity.workerDeploymentRole = workerDeploymentRole;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.shift = shift;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long employeeId,
        Long equipmentId,
        String workerDeploymentRole,
        LocalDate startDate,
        LocalDate endDate,
        String shift,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.employeeId = employeeId;
        this.equipmentId = equipmentId;
        this.workerDeploymentRole = workerDeploymentRole;
        this.startDate = startDate;
        this.endDate = endDate;
        this.shift = shift;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
