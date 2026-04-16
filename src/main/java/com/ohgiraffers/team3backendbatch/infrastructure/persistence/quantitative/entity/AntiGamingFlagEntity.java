package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

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
@Table(name = "anti_gaming_flag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AntiGamingFlagEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "flag_id")
    private Long flagId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "production_speed_rank")
    private Integer productionSpeedRank;

    @Column(name = "safety_keyword_rank")
    private Integer safetyKeywordRank;

    @Column(name = "penalty_coefficient")
    private BigDecimal penaltyCoefficient;

    @Column(name = "target_year")
    private Integer targetYear;

    @Column(name = "target_period")
    private String targetPeriod;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static AntiGamingFlagEntity create(
        Long flagId,
        Long employeeId,
        Integer productionSpeedRank,
        Integer safetyKeywordRank,
        BigDecimal penaltyCoefficient,
        Integer targetYear,
        String targetPeriod,
        LocalDateTime now
    ) {
        AntiGamingFlagEntity entity = new AntiGamingFlagEntity();
        entity.flagId = flagId;
        entity.employeeId = employeeId;
        entity.productionSpeedRank = productionSpeedRank;
        entity.safetyKeywordRank = safetyKeywordRank;
        entity.penaltyCoefficient = penaltyCoefficient;
        entity.targetYear = targetYear;
        entity.targetPeriod = targetPeriod;
        entity.isActive = Boolean.TRUE;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refresh(
        Integer productionSpeedRank,
        Integer safetyKeywordRank,
        BigDecimal penaltyCoefficient,
        LocalDateTime now
    ) {
        this.productionSpeedRank = productionSpeedRank;
        this.safetyKeywordRank = safetyKeywordRank;
        this.penaltyCoefficient = penaltyCoefficient;
        this.isActive = Boolean.TRUE;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }

    public void deactivate(LocalDateTime now) {
        this.isActive = Boolean.FALSE;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
