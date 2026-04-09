package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentStandardSnapshotEvent {

    private Long environmentStandardId;
    private String environmentType;
    private String environmentCode;
    private String environmentName;
    private BigDecimal envTempMin;
    private BigDecimal envTempMax;
    private BigDecimal envHumidityMin;
    private BigDecimal envHumidityMax;
    private Integer envParticleLimit;
    private Boolean deleted;
    private LocalDateTime occurredAt;
}
