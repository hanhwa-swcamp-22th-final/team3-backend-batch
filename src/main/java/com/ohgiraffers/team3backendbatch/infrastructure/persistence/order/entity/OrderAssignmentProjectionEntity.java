package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_assignment_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderAssignmentProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "matching_record_id")
    private Long matchingRecordId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "matching_status")
    private String matchingStatus;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

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

    public static OrderAssignmentProjectionEntity create(
        Long matchingRecordId,
        Long orderId,
        Long employeeId,
        String matchingStatus,
        LocalDateTime assignedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        OrderAssignmentProjectionEntity entity = new OrderAssignmentProjectionEntity();
        entity.matchingRecordId = matchingRecordId;
        entity.orderId = orderId;
        entity.employeeId = employeeId;
        entity.matchingStatus = matchingStatus;
        entity.assignedAt = assignedAt;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long orderId,
        Long employeeId,
        String matchingStatus,
        LocalDateTime assignedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.matchingStatus = matchingStatus;
        this.assignedAt = assignedAt;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
