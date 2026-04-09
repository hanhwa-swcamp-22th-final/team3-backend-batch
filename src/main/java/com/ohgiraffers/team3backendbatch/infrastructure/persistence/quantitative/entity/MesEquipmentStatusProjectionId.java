package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MesEquipmentStatusProjectionId implements Serializable {

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "status_type")
    private String statusType;

    @Column(name = "start_time_stamp")
    private LocalDateTime startTimeStamp;
}
