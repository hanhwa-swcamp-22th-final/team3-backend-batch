package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "employee_tier")
    private String employeeTier;

    @Column(name = "employee_status")
    private String employeeStatus;

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

    public static EmployeeProjectionEntity create(
        Long employeeId,
        String employeeCode,
        String employeeTier,
        String employeeStatus,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        EmployeeProjectionEntity entity = new EmployeeProjectionEntity();
        entity.employeeId = employeeId;
        entity.employeeCode = employeeCode;
        entity.employeeTier = employeeTier;
        entity.employeeStatus = employeeStatus;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String employeeCode,
        String employeeTier,
        String employeeStatus,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.employeeCode = employeeCode;
        this.employeeTier = employeeTier;
        this.employeeStatus = employeeStatus;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
