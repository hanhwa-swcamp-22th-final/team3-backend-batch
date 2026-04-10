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
@Table(catalog = "batch_projection", name = "environment_standard_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvironmentStandardProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "environment_standard_id")
    private Long environmentStandardId;

    @Column(name = "environment_type")
    private String environmentType;

    @Column(name = "environment_code")
    private String environmentCode;

    @Column(name = "environment_name")
    private String environmentName;

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

    public static EnvironmentStandardProjectionEntity create(
        Long environmentStandardId,
        String environmentType,
        String environmentCode,
        String environmentName,
        BigDecimal envTempMin,
        BigDecimal envTempMax,
        BigDecimal envHumidityMin,
        BigDecimal envHumidityMax,
        Integer envParticleLimit,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        EnvironmentStandardProjectionEntity entity = new EnvironmentStandardProjectionEntity();
        entity.environmentStandardId = environmentStandardId;
        entity.refreshSnapshot(
            environmentType,
            environmentCode,
            environmentName,
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
        String environmentType,
        String environmentCode,
        String environmentName,
        BigDecimal envTempMin,
        BigDecimal envTempMax,
        BigDecimal envHumidityMin,
        BigDecimal envHumidityMax,
        Integer envParticleLimit,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.environmentType = environmentType;
        this.environmentCode = environmentCode;
        this.environmentName = environmentName;
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
