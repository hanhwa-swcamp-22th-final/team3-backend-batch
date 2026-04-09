package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderDifficultyAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.OrderDifficultyKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDifficultyAnalysisEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderDifficultyAnalysisEventPublisher.class);

    private final KafkaTemplate<String, OrderDifficultyAnalyzedEvent> orderDifficultyAnalyzedKafkaTemplate;

    public void publishAnalyzed(OrderDifficultyAnalyzedEvent event) {
        orderDifficultyAnalyzedKafkaTemplate.send(
            OrderDifficultyKafkaTopics.ORDER_DIFFICULTY_ANALYZED,
            String.valueOf(event.getOrderId()),
            event
        );
        log.info(
            "Published order difficulty analyzed event. orderId={}, grade={}, score={}",
            event.getOrderId(),
            event.getDifficultyGrade(),
            event.getDifficultyScore()
        );
    }
}
