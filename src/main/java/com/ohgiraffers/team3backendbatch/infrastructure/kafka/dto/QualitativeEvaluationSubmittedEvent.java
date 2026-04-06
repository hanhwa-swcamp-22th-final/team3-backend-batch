package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeEvaluationSubmittedEvent {

    private Long qualitativeEvaluationId;
    private Long evaluationPeriodId;
    private Long evaluateeId;
    private Long evaluatorId;
    private Long evaluationLevel;
    private String status;
    private LocalDateTime occurredAt;
}