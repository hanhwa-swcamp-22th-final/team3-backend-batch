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
public class MesProductionResultEvent {

    private String eventId;
    private Long equipmentId;
    private String sourceEquipmentCode;
    private String equipmentNameSnapshot;
    private String inputLotNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal cycleTimeSec;
    private BigDecimal leadTimeSec;
    private BigDecimal inputQty;
    private BigDecimal outputQty;
    private BigDecimal goodQty;
    private BigDecimal defectQty;
    private LocalDateTime occurredAt;
}
