package com.ohgiraffers.team3backendbatch.batch.job.quantitative.reader;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

/**
 * 정량 평가 계산에 필요한 raw 원천 DB 데이터를 읽는 Reader 스켈레톤이다.
 *
 * 예상 입력 소스:
 * - mes_production_result
 * - mes_quality_result
 * - mes_equipment_status
 * - worker_deployment
 * - evaluation_period
 * - algorithm_version
 *
 * 구현 예정 메서드/내용:
 * - readWeeklyPreviewSources(...)
 * - readMonthlySettlementSources(...)
 * - aggregateRawLotResults(...)
 * - filterAlreadySettledSources(...)
 *
 * 주의:
 * - QUARTER/HALF_YEAR/YEAR summary 는 이 Reader 를 직접 사용하지 않는 것이 원칙이다.
 * - 상위 기간 summary 는 월간 settlement 결과를 읽는 전용 Reader 에서 처리한다.
 */
@Component
public class QuantitativeSourceReader implements ItemReader<QuantitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeSourceReader.class);

    private boolean logged;

    @Override
    public QuantitativeEvaluationAggregate read() {
        if (!logged) {
            logged = true;
            log.info("QuantitativeSourceReader chunk skeleton invoked. TODO replace with DB-backed reader.");
        }
        return null;
    }
}