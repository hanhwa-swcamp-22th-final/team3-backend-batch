package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDifficultySource {
    private Long orderId;
    private Long productId;
    private Long configId;
    private String orderNumber;
    private Integer orderQuantity;
    private Integer processStepCount;
    private BigDecimal toleranceMm;
    private Integer skillLevel;
    private LocalDate referenceDate;
    private LocalDate dueDate;
    private Boolean firstOrder;
    private String industryPreset;
    private BigDecimal weightV1;
    private BigDecimal weightV2;
    private BigDecimal weightV3;
    private BigDecimal weightV4;
    private BigDecimal alphaWeight;
    private String productName;
    private String productCode;
}
