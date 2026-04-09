package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDifficultySnapshotEvent {
    private Long orderId;
    private BigDecimal difficultyScore;
    private String difficultyGrade;
    private String orderStatus;
    private LocalDateTime analyzedAt;
    private LocalDateTime occurredAt;
}
