package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSnapshotEvent {
    private Long matchingRecordId;
    private Long orderId;
    private Long employeeId;
    private String matchingStatus;
    private LocalDateTime assignedAt;
    private LocalDateTime occurredAt;
}
