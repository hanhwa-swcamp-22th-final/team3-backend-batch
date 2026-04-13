package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationPeriodSnapshotEvent {

    private Long evaluationPeriodId;
    private Long algorithmVersionId;
    private Integer evaluationYear;
    private Integer evaluationSequence;
    private String evaluationType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String algorithmVersionNo;
    private String algorithmImplementationKey;
    private String policyConfig;
    private String parameters;
    private String referenceValues;
    private LocalDateTime occurredAt;
}
