package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "performance_point_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformancePointProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "performance_point_id")
    private Long performancePointId;

    @Column(name = "performance_employee_id", nullable = false)
    private Long performanceEmployeeId;

    @Column(name = "point_type")
    private String pointType;

    @Column(name = "point_amount")
    private BigDecimal pointAmount;

    @Column(name = "point_earned_date")
    private LocalDate pointEarnedDate;

    @Column(name = "point_source_id")
    private Long pointSourceId;

    @Column(name = "point_source_type")
    private String pointSourceType;

    @Column(name = "point_description")
    private String pointDescription;

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

    public static PerformancePointProjectionEntity create(
        Long performancePointId,
        Long performanceEmployeeId,
        String pointType,
        BigDecimal pointAmount,
        LocalDate pointEarnedDate,
        Long pointSourceId,
        String pointSourceType,
        String pointDescription,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        PerformancePointProjectionEntity entity = new PerformancePointProjectionEntity();
        entity.performancePointId = performancePointId;
        entity.performanceEmployeeId = performanceEmployeeId;
        entity.pointType = pointType;
        entity.pointAmount = pointAmount;
        entity.pointEarnedDate = pointEarnedDate;
        entity.pointSourceId = pointSourceId;
        entity.pointSourceType = pointSourceType;
        entity.pointDescription = pointDescription;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long performanceEmployeeId,
        String pointType,
        BigDecimal pointAmount,
        LocalDate pointEarnedDate,
        Long pointSourceId,
        String pointSourceType,
        String pointDescription,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.performanceEmployeeId = performanceEmployeeId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.pointEarnedDate = pointEarnedDate;
        this.pointSourceId = pointSourceId;
        this.pointSourceType = pointSourceType;
        this.pointDescription = pointDescription;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
