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
@Table(name = "equipment_reference_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentReferenceProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "equipment_code")
    private String equipmentCode;

    @Column(name = "equipment_status")
    private String equipmentStatus;

    @Column(name = "equipment_grade")
    private String equipmentGrade;

    @Column(name = "equipment_install_date")
    private LocalDateTime equipmentInstallDate;

    @Column(name = "environment_standard_id")
    private Long environmentStandardId;

    @Column(name = "equipment_warranty_month")
    private Integer equipmentWarrantyMonth;

    @Column(name = "equipment_design_life_months")
    private Integer equipmentDesignLifeMonths;

    @Column(name = "equipment_wear_coefficient")
    private BigDecimal equipmentWearCoefficient;

    @Column(name = "equipment_standard_performance_rate")
    private BigDecimal equipmentStandardPerformanceRate;

    @Column(name = "equipment_baseline_error_rate")
    private BigDecimal equipmentBaselineErrorRate;

    @Column(name = "equipment_eta_maint")
    private BigDecimal equipmentEtaMaint;

    @Column(name = "equipment_idx")
    private BigDecimal equipmentIdx;

    @Column(name = "env_temp_min")
    private BigDecimal envTempMin;

    @Column(name = "env_temp_max")
    private BigDecimal envTempMax;

    @Column(name = "env_humidity_min")
    private BigDecimal envHumidityMin;

    @Column(name = "env_humidity_max")
    private BigDecimal envHumidityMax;

    @Column(name = "env_particle_limit")
    private Integer envParticleLimit;

    @Column(name = "deleted")
    private Boolean deleted;

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

    public static EquipmentReferenceProjectionEntity create(
        Long equipmentId,
        String equipmentCode,
        String equipmentStatus,
        String equipmentGrade,
        LocalDateTime equipmentInstallDate,
        Long environmentStandardId,
        Integer equipmentWarrantyMonth,
        Integer equipmentDesignLifeMonths,
        BigDecimal equipmentWearCoefficient,
        BigDecimal equipmentStandardPerformanceRate,
        BigDecimal equipmentBaselineErrorRate,
        BigDecimal equipmentEtaMaint,
        BigDecimal equipmentIdx,
        BigDecimal envTempMin,
        BigDecimal envTempMax,
        BigDecimal envHumidityMin,
        BigDecimal envHumidityMax,
        Integer envParticleLimit,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        EquipmentReferenceProjectionEntity entity = new EquipmentReferenceProjectionEntity();
        entity.equipmentId = equipmentId;
        entity.refreshSnapshot(
            equipmentCode,
            equipmentStatus,
            equipmentGrade,
            equipmentInstallDate,
            environmentStandardId,
            equipmentWarrantyMonth,
            equipmentDesignLifeMonths,
            equipmentWearCoefficient,
            equipmentStandardPerformanceRate,
            equipmentBaselineErrorRate,
            equipmentEtaMaint,
            equipmentIdx,
            envTempMin,
            envTempMax,
            envHumidityMin,
            envHumidityMax,
            envParticleLimit,
            deleted,
            occurredAt,
            now
        );
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String equipmentCode,
        String equipmentStatus,
        String equipmentGrade,
        LocalDateTime equipmentInstallDate,
        Long environmentStandardId,
        Integer equipmentWarrantyMonth,
        Integer equipmentDesignLifeMonths,
        BigDecimal equipmentWearCoefficient,
        BigDecimal equipmentStandardPerformanceRate,
        BigDecimal equipmentBaselineErrorRate,
        BigDecimal equipmentEtaMaint,
        BigDecimal equipmentIdx,
        BigDecimal envTempMin,
        BigDecimal envTempMax,
        BigDecimal envHumidityMin,
        BigDecimal envHumidityMax,
        Integer envParticleLimit,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentCode = equipmentCode;
        this.equipmentStatus = equipmentStatus;
        this.equipmentGrade = equipmentGrade;
        this.equipmentInstallDate = equipmentInstallDate;
        this.environmentStandardId = environmentStandardId;
        this.equipmentWarrantyMonth = equipmentWarrantyMonth;
        this.equipmentDesignLifeMonths = equipmentDesignLifeMonths;
        this.equipmentWearCoefficient = equipmentWearCoefficient;
        this.equipmentStandardPerformanceRate = equipmentStandardPerformanceRate;
        this.equipmentBaselineErrorRate = equipmentBaselineErrorRate;
        this.equipmentEtaMaint = equipmentEtaMaint;
        this.equipmentIdx = equipmentIdx;
        this.envTempMin = envTempMin;
        this.envTempMax = envTempMax;
        this.envHumidityMin = envHumidityMin;
        this.envHumidityMax = envHumidityMax;
        this.envParticleLimit = envParticleLimit;
        this.deleted = deleted;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
