package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "order_difficulty_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDifficultyProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "difficulty_score")
    private BigDecimal difficultyScore;

    @Column(name = "difficulty_grade")
    private String difficultyGrade;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

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

    public static OrderDifficultyProjectionEntity create(
        Long orderId,
        BigDecimal difficultyScore,
        String difficultyGrade,
        String orderStatus,
        LocalDateTime analyzedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        OrderDifficultyProjectionEntity entity = new OrderDifficultyProjectionEntity();
        entity.orderId = orderId;
        entity.difficultyScore = difficultyScore;
        entity.difficultyGrade = difficultyGrade;
        entity.orderStatus = orderStatus;
        entity.analyzedAt = analyzedAt;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        BigDecimal difficultyScore,
        String difficultyGrade,
        String orderStatus,
        LocalDateTime analyzedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.difficultyScore = difficultyScore;
        this.difficultyGrade = difficultyGrade;
        this.orderStatus = orderStatus;
        this.analyzedAt = analyzedAt;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
