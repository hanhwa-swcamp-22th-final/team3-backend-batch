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
