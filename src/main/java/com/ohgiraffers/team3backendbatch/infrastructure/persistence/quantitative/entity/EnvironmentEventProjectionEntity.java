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
@Table(catalog = "batch_projection", name = "environment_event_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvironmentEventProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "environment_event_id")
    private String environmentEventId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "environment_standard_id")
    private Long environmentStandardId;

    @Column(name = "source_equipment_code")
    private String sourceEquipmentCode;

    @Column(name = "env_temperature")
    private BigDecimal envTemperature;

    @Column(name = "env_humidity")
    private BigDecimal envHumidity;

    @Column(name = "env_particle_cnt")
    private Integer envParticleCnt;

    @Column(name = "env_deviation_type")
    private String envDeviationType;

    @Column(name = "env_correction_applied")
    private Boolean envCorrectionApplied;

    @Column(name = "env_detected_at")
    private LocalDateTime envDetectedAt;

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

    public static EnvironmentEventProjectionEntity create(
        String environmentEventId,
        Long equipmentId,
        Long environmentStandardId,
        String sourceEquipmentCode,
        BigDecimal envTemperature,
        BigDecimal envHumidity,
        Integer envParticleCnt,
        String envDeviationType,
        Boolean envCorrectionApplied,
        LocalDateTime envDetectedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        EnvironmentEventProjectionEntity entity = new EnvironmentEventProjectionEntity();
        entity.environmentEventId = environmentEventId;
        entity.refreshSnapshot(
            equipmentId,
            environmentStandardId,
            sourceEquipmentCode,
            envTemperature,
            envHumidity,
            envParticleCnt,
            envDeviationType,
            envCorrectionApplied,
            envDetectedAt,
            occurredAt,
            now
        );
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long equipmentId,
        Long environmentStandardId,
        String sourceEquipmentCode,
        BigDecimal envTemperature,
        BigDecimal envHumidity,
        Integer envParticleCnt,
        String envDeviationType,
        Boolean envCorrectionApplied,
        LocalDateTime envDetectedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentId = equipmentId;
        this.environmentStandardId = environmentStandardId;
        this.sourceEquipmentCode = sourceEquipmentCode;
        this.envTemperature = envTemperature;
        this.envHumidity = envHumidity;
        this.envParticleCnt = envParticleCnt;
        this.envDeviationType = envDeviationType;
        this.envCorrectionApplied = envCorrectionApplied;
        this.envDetectedAt = envDetectedAt;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
