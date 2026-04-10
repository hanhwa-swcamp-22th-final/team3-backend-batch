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
public class MesQualityResultEvent {

    private Long qualityResultId;
    private String prodLotNo;
    private Long equipmentId;
    private String sourceEquipmentCode;
    private String inputLotNo;
    private LocalDateTime eventTimeStamp;
    private String overallResult;
    private LocalDateTime occurredAt;
}
