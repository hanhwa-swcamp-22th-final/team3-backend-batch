package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PromotionCandidateEvaluatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.PromotionKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionCandidateEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PromotionCandidateEventPublisher.class);

    private final KafkaTemplate<String, PromotionCandidateEvaluatedEvent> promotionCandidateEvaluatedKafkaTemplate;

    public void publishEvaluated(PromotionCandidateEvaluatedEvent event) {
        String key = event.getEmployeeId() + ":" + event.getCurrentTierConfigId() + ":" + event.getTargetTierConfigId();
        promotionCandidateEvaluatedKafkaTemplate.send(
            PromotionKafkaTopics.PROMOTION_CANDIDATE_EVALUATED,
            key,
            event
        );
        log.info(
            "Published promotion candidate event. employeeId={}, currentTier={}, targetTier={}, accumulatedPoint={}",
            event.getEmployeeId(),
            event.getCurrentTier(),
            event.getTargetTier(),
            event.getTierAccumulatedPoint()
        );
    }
}
