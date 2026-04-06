package com.ohgiraffers.team3backendbatch.batch.job.promotion.processor;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 승진 후보 여부를 판단하는 Processor 스켈레톤이다.
 *
 * 예상 기능:
 * - 누적 tier 점수가 승진 기준 점수를 넘는지 비교
 * - 현재 tier 대비 다음 tier 결정
 * - 합격 가능 여부와 보류 조건 분기
 * - HR 검토용 promotion_history 입력값 생성
 */
@Component
public class PromotionCandidateProcessor
    implements ItemProcessor<PromotionCandidateSnapshot, PromotionCandidateSnapshot> {

    @Override
    public PromotionCandidateSnapshot process(PromotionCandidateSnapshot item) {
        // TODO PromotionRuleEvaluator를 사용해 후보 판정
        return item;
    }
}