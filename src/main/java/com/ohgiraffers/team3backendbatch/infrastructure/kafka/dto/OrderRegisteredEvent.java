package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRegisteredEvent {
    private Long orderId;
    private Long productId;
    private Long configId;
    private String orderNumber;
    private Integer orderQuantity;
    private Integer processStepCount;
    private BigDecimal toleranceMm;
    private Integer skillLevel;
    private LocalDate dueDate;
    private Boolean firstOrder;
    private String productName;
    private String productCode;
    private String industryPreset;
    private BigDecimal weightV1;
    private BigDecimal weightV2;
    private BigDecimal weightV3;
    private BigDecimal weightV4;
    private BigDecimal alphaWeight;
    private LocalDateTime occurredAt;

    public OrderRegisteredEvent(
        Long orderId,
        Long productId,
        Long configId,
        String orderNumber,
        Integer orderQuantity,
        LocalDate dueDate,
        Boolean firstOrder,
        LocalDateTime occurredAt
    ) {
        this(
            orderId,
            productId,
            configId,
            orderNumber,
            orderQuantity,
            null,
            null,
            null,
            dueDate,
            firstOrder,
            occurredAt
        );
    }

    public OrderRegisteredEvent(
        Long orderId,
        Long productId,
        Long configId,
        String orderNumber,
        Integer orderQuantity,
        Integer processStepCount,
        BigDecimal toleranceMm,
        Integer skillLevel,
        LocalDate dueDate,
        Boolean firstOrder,
        LocalDateTime occurredAt
    ) {
        this.orderId = orderId;
        this.productId = productId;
        this.configId = configId;
        this.orderNumber = orderNumber;
        this.orderQuantity = orderQuantity;
        this.processStepCount = processStepCount;
        this.toleranceMm = toleranceMm;
        this.skillLevel = skillLevel;
        this.dueDate = dueDate;
        this.firstOrder = firstOrder;
        this.occurredAt = occurredAt;
    }
}
