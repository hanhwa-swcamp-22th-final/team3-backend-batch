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
public class MesEnvironmentEvent {

    private String eventId;
    private Long equipmentId;
    private String sourceEquipmentCode;
    private BigDecimal envTemperature;
    private BigDecimal envHumidity;
    private Integer envParticleCnt;
    private LocalDateTime envDetectedAt;
    private LocalDateTime occurredAt;
}
