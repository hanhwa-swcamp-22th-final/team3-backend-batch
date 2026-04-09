package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

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
public class MesEquipmentStatusEvent {

    private Long equipmentId;
    private String sourceEquipmentCode;
    private String statusType;
    private LocalDateTime startTimeStamp;
    private LocalDateTime endTimeStamp;
    private String alarmCode;
    private String alarmDesc;
    private LocalDateTime occurredAt;
}
