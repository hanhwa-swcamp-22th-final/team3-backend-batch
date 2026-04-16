package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.processor;

import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionResult;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionTarget;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PeriodSettlementInspectionProcessor
    implements ItemProcessor<PeriodSettlementInspectionTarget, PeriodSettlementInspectionResult> {

    private static final BigDecimal SWING_THRESHOLD = new BigDecimal("25.00");

    /**
     * 상위 기간 정산 점검 결과를 생성한다.
     * @param item 상위 기간 정산 점검 대상 데이터
     * @return 상위 기간 정산 점검 결과
     */
    @Override
    public PeriodSettlementInspectionResult process(PeriodSettlementInspectionTarget item) {
        List<String> findings = new ArrayList<>();

        if (item.getQuantitativeMonthCount() < item.getExpectedMonthCount()) {
            findings.add("Missing confirmed monthly quantitative settlements: "
                + item.getQuantitativeMonthCount() + "/" + item.getExpectedMonthCount());
        }

        if (item.getQualitativeMonthCount() < item.getExpectedMonthCount()) {
            findings.add("Missing confirmed monthly qualitative normalizations: "
                + item.getQualitativeMonthCount() + "/" + item.getExpectedMonthCount());
        }

        if (hasLargeSwing(item.getQuantitativeMinScore(), item.getQuantitativeMaxScore())) {
            findings.add("Large quantitative monthly swing detected: "
                + item.getQuantitativeMinScore() + " -> " + item.getQuantitativeMaxScore());
        }

        if (hasLargeSwing(item.getQualitativeMinScore(), item.getQualitativeMaxScore())) {
            findings.add("Large qualitative monthly swing detected: "
                + item.getQualitativeMinScore() + " -> " + item.getQualitativeMaxScore());
        }

        if (item.getPerformancePointTotal() == null || item.getPerformancePointTotal().compareTo(BigDecimal.ZERO) <= 0) {
            findings.add("No performance point accumulation detected inside target period.");
        }

        String inspectionStatus = findings.isEmpty() ? "OK" : "WARN";
        return PeriodSettlementInspectionResult.builder()
            .evaluationPeriodId(item.getEvaluationPeriodId())
            .periodType(item.getPeriodType())
            .employeeId(item.getEmployeeId())
            .expectedMonthCount(item.getExpectedMonthCount())
            .quantitativeMonthCount(item.getQuantitativeMonthCount())
            .qualitativeMonthCount(item.getQualitativeMonthCount())
            .inspectionStatus(inspectionStatus)
            .findings(List.copyOf(findings))
            .build();
    }

    /**
     * 최소/최대 점수 차이가 임계값 이상인지 확인한다.
     * @param minScore 최소 점수
     * @param maxScore 최대 점수
     * @return 변동 폭 임계값 초과 여부
     */
    private boolean hasLargeSwing(BigDecimal minScore, BigDecimal maxScore) {
        if (minScore == null || maxScore == null) {
            return false;
        }
        return maxScore.subtract(minScore).compareTo(SWING_THRESHOLD) >= 0;
    }
}
