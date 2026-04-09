package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentEventSnapshotEvent {

    private Long environmentEventId;
    private Long equipmentId;
    private BigDecimal envTemperature;
    private BigDecimal envHumidity;
    private Integer envParticleCnt;
    private String envDeviationType;
    private Boolean envCorrectionApplied;
    private LocalDateTime envDetectedAt;
    private Boolean deleted;
    private LocalDateTime occurredAt;
}
