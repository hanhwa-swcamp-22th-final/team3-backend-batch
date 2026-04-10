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
public class MesQualityMeasurementEvent {

    private Long qualityResultId;
    private String processCode;
    private String measureItem;
    private String prodLotNo;
    private String inputLotNo;
    private BigDecimal ucl;
    private BigDecimal targetValue;
    private BigDecimal lcl;
    private BigDecimal measuredValue;
    private String judgeResult;
    private LocalDateTime occurredAt;
}
