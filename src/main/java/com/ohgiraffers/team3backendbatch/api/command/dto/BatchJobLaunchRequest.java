package com.ohgiraffers.team3backendbatch.api.command.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Manual batch launch request DTO.
 *
 * Intended usage:
 * - determine execution window from mode + periodType + evaluationPeriodId
 * - pass employeeId when launching employee-scoped jobs
 * - pass qualitativeEvaluationId when launching a single qualitative evaluation record
 * - when force=true, duplicate execution checks for the same target may be bypassed by policy
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobLaunchRequest {
    private ManualJobLaunchMode mode;
    private BatchPeriodType periodType;
    private Long evaluationPeriodId;
    private Long employeeId;
    private Long qualitativeEvaluationId;
    private Boolean force;
    @NotBlank
    private String requestedBy;
    @NotBlank
    private String reason;
}