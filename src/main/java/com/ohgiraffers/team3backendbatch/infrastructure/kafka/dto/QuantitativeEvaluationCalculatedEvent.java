package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
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
public class QuantitativeEvaluationCalculatedEvent {

    private Long employeeId;
    private Long evaluationPeriodId;
    private Long algorithmVersionId;
    private String periodType;
    private LocalDateTime calculatedAt;
    private List<QuantitativeEquipmentResultEvent> equipmentResults;
}
