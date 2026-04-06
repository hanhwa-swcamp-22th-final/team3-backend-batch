package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PromotionRuleEvaluator {

    /**
     * 공식 누적 tier 점수가 승진 후보 기준을 충족했는지 판정한다.
     *
     * 구현 메모:
     * - 주간 preview 값은 승진 판정에서 제외한다.
     * - 월간 settlement 로 확정된 누적 점수 또는 상위 기간 summary 를 기준으로만 평가한다.
     *
     * 추후 추가 예정 메서드/내용:
     * - isPromotionCandidateForMonthlySettlement(...)
     * - isPromotionCandidateForQuarterSummary(...)
     * - hasRecentPromotionCooldown(...)
     * - isBlockedByHoldStatus(...)
     */
    public boolean isPromotionCandidate(BigDecimal tierAccumulatedPoint, int promotionThreshold) {
        return tierAccumulatedPoint != null
            && tierAccumulatedPoint.compareTo(BigDecimal.valueOf(promotionThreshold)) >= 0;
    }
}
