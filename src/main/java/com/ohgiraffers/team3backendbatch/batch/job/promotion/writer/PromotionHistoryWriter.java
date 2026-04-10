package com.ohgiraffers.team3backendbatch.batch.job.promotion.writer;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PromotionCandidateEvaluatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.PromotionCandidateEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionHistoryWriter implements ItemWriter<PromotionCandidateSnapshot> {

    private static final Logger log = LoggerFactory.getLogger(PromotionHistoryWriter.class);
    private static final String DEFAULT_PROMOTION_STATUS = "UNDER_REVIEW";

    private final PromotionCandidateEventPublisher promotionCandidateEventPublisher;

    @Override
    public void write(Chunk<? extends PromotionCandidateSnapshot> chunk) {
        int eventCount = 0;
        for (PromotionCandidateSnapshot item : chunk.getItems()) {
            PromotionCandidateEvaluatedEvent event = PromotionCandidateEvaluatedEvent.builder()
                .employeeId(item.getEmployeeId())
                .evaluationPeriodId(item.getEvaluationPeriodId())
                .periodType(item.getPeriodType())
                .currentTier(item.getCurrentTier())
                .targetTier(item.getTargetTier())
                .currentTierConfigId(item.getCurrentTierConfigId())
                .targetTierConfigId(item.getTargetTierConfigId())
                .tierAccumulatedPoint(item.getTierAccumulatedPoint())
                .promotionThreshold(item.getPromotionThreshold())
                .tierConfigEffectiveDate(item.getTierConfigEffectiveDate())
                .promotionStatus(DEFAULT_PROMOTION_STATUS)
                .occurredAt(item.getOccurredAt())
                .build();
            promotionCandidateEventPublisher.publishEvaluated(event);
            eventCount++;
        }
        log.info("Published promotion candidate events. itemCount={}, eventCount={}", chunk.size(), eventCount);
    }
}
