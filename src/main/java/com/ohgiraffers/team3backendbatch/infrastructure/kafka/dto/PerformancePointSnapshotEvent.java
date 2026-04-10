package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
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
public class PerformancePointSnapshotEvent {

    private Long performancePointId;
    private Long employeeId;
    private String pointType;
    private BigDecimal pointAmount;
    private LocalDate pointEarnedDate;
    private Long pointSourceId;
    private String pointSourceType;
    private String pointDescription;
    private LocalDateTime occurredAt;
}
