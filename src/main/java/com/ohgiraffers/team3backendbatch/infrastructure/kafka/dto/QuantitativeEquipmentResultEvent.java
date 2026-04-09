package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
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
public class QuantitativeEquipmentResultEvent {

    private Long equipmentId;
    private BigDecimal uphScore;
    private BigDecimal yieldScore;
    private BigDecimal leadTimeScore;
    private BigDecimal actualError;
    private BigDecimal sQuant;
    private BigDecimal tScore;
    private Integer materialShielding;
    private String status;
}
