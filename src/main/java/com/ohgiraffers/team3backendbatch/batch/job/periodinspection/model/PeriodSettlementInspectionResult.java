package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PeriodSettlementInspectionResult {

    private final Long evaluationPeriodId;
    private final BatchPeriodType periodType;
    private final Long employeeId;
    private final int expectedMonthCount;
    private final int quantitativeMonthCount;
    private final int qualitativeMonthCount;
    private final String inspectionStatus;
    private final List<String> findings;
}
