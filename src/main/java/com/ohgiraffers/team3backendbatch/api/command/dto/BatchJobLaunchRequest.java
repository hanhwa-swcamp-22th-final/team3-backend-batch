package com.ohgiraffers.team3backendbatch.api.command.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobLaunchRequest {
    private ManualJobLaunchMode mode;
    private BatchPeriodType periodType;
    private Long evaluationPeriodId;
    private Long employeeId;
    private Long orderId;
    private LocalDate analysisReferenceDate;
    private String orderEventPayload;
    private Long qualitativeEvaluationId;
    private String qualitativeEventPayload;
    private Boolean force;
    @NotBlank
    private String requestedBy;
    @NotBlank
    private String reason;
}
