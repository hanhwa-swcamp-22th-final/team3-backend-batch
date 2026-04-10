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
public class TierConfigSnapshotEvent {

    private Long tierConfigId;
    private String tier;
    private String weightDistribution;
    private Integer promotionPoint;
    private LocalDateTime occurredAt;
}
