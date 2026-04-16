package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.writer;

import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class PeriodSettlementInspectionWriter implements ItemWriter<PeriodSettlementInspectionResult> {

    private static final Logger log = LoggerFactory.getLogger(PeriodSettlementInspectionWriter.class);

    /**
     * 상위 기간 정산 점검 결과를 로그로 출력한다.
     * @param chunk 기록할 상위 기간 정산 점검 결과 묶음
     * @return 반환값 없음
     */
    @Override
    public void write(Chunk<? extends PeriodSettlementInspectionResult> chunk) {
        int warnCount = 0;
        for (PeriodSettlementInspectionResult item : chunk.getItems()) {
            if ("WARN".equalsIgnoreCase(item.getInspectionStatus())) {
                warnCount++;
                log.warn(
                    "Upper-period settlement inspection warning. evaluationPeriodId={}, periodType={}, employeeId={}, findings={}",
                    item.getEvaluationPeriodId(),
                    item.getPeriodType(),
                    item.getEmployeeId(),
                    item.getFindings()
                );
                continue;
            }

            log.info(
                "Upper-period settlement inspection passed. evaluationPeriodId={}, periodType={}, employeeId={}, expectedMonthCount={}, quantitativeMonthCount={}, qualitativeMonthCount={}",
                item.getEvaluationPeriodId(),
                item.getPeriodType(),
                item.getEmployeeId(),
                item.getExpectedMonthCount(),
                item.getQuantitativeMonthCount(),
                item.getQualitativeMonthCount()
            );
        }

        log.info("Completed upper-period settlement inspection chunk. itemCount={}, warningCount={}", chunk.size(), warnCount);
    }
}
