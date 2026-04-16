package com.ohgiraffers.team3backendbatch.batch.job.promotion.processor;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import com.ohgiraffers.team3backendbatch.domain.scoring.PromotionRuleEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionCandidateProcessor
    implements ItemProcessor<PromotionCandidateSnapshot, PromotionCandidateSnapshot> {

    private final PromotionRuleEvaluator promotionRuleEvaluator;

    /**
     * 승급 기준 충족 여부를 판정한다.
     * @param item 승급 후보 스냅샷
     * @return 승급 대상인 경우 동일 스냅샷, 아니면 null
     */
    @Override
    public PromotionCandidateSnapshot process(PromotionCandidateSnapshot item) {
        if (item == null || item.getPromotionThreshold() == null || item.getTierAccumulatedPoint() == null) {
            return null;
        }

        return promotionRuleEvaluator.isPromotionCandidate(
            item.getTierAccumulatedPoint(),
            item.getPromotionThreshold()
        ) ? item : null;
    }
}
