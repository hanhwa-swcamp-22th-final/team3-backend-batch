package com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 정량 평가 결과를 quantitative_evaluation 테이블에 반영하는 Writer 스켈레톤이다.
 *
 * 예상 기능:
 * - evaluation period + employee + equipment 기준 upsert
 * - 수동 실행 시 동일 키 충돌 처리
 * - created/updated audit 값 반영
 * - 필요 시 performance_point 상세 이력과 함께 트랜잭션 처리
 */
@Component
public class QuantitativeEvaluationWriter implements ItemWriter<QuantitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeEvaluationWriter.class);

    @Override
    public void write(Chunk<? extends QuantitativeEvaluationAggregate> chunk) {
        log.info("QuantitativeEvaluationWriter chunk skeleton invoked. itemCount={}", chunk.size());
        // TODO JPA batch insert 또는 MyBatis bulk upsert 구현
    }
}