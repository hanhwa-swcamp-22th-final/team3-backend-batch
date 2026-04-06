package com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 정량 평가 집계값을 점수 레코드로 변환하는 Processor 스켈레톤이다.
 *
 * 예상 기능:
 * - uph_score 계산
 * - yield_score 계산
 * - lead_time_score 계산
 * - e_idx 계산
 * - baseline_error / actual_error 계산
 * - s_quant 계산
 * - t_score 계산
 * - status 를 초안/확정 중 어떤 값으로 넣을지 결정
 *
 * 공식 자체는 별도 FormulaService 또는 domain calculator 에 위임하고,
 * 이 Processor 는 조합과 흐름 제어를 집중하는 구조가 적절하다.
 */
@Component
public class QuantitativeEvaluationProcessor
    implements ItemProcessor<QuantitativeEvaluationAggregate, QuantitativeEvaluationAggregate> {

    @Override
    public QuantitativeEvaluationAggregate process(QuantitativeEvaluationAggregate item) {
        // TODO FormulaService를 이용해 정량 평가 DTO 또는 entity 생성
        return item;
    }
}