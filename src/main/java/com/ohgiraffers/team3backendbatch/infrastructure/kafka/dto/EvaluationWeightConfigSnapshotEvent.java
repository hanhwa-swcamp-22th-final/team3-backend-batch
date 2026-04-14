package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationWeightConfigSnapshotEvent {

    private Long evaluationWeightConfigId;
    private String tierGroup;
    private String categoryCode;
    private Integer weightPercent;
    private Boolean active;
    private Boolean deleted;
    private LocalDateTime occurredAt;
}
