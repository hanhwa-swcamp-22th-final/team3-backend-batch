package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
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
public class MesQualityMeasurementProjectionId implements Serializable {

    @Column(name = "quality_result_id")
    private Long qualityResultId;

    @Column(name = "process_code")
    private String processCode;

    @Column(name = "measure_item")
    private String measureItem;
}
