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
public class MaintenanceItemStandardSnapshotEvent {

    private Long maintenanceItemStandardId;
    private String maintenanceItem;
    private BigDecimal maintenanceWeight;
    private BigDecimal maintenanceScoreMax;
    private Boolean deleted;
    private LocalDateTime occurredAt;
}
