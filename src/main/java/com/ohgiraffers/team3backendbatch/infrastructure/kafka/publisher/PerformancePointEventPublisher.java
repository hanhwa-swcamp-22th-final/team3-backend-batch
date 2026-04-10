package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.PromotionKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformancePointEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PerformancePointEventPublisher.class);

    private final KafkaTemplate<String, PerformancePointCalculatedEvent> performancePointCalculatedKafkaTemplate;

    public void publishCalculated(PerformancePointCalculatedEvent event) {
        String key = event.getEmployeeId() + ":" + event.getPointType() + ":" + event.getPointSourceId();
        performancePointCalculatedKafkaTemplate.send(
            PromotionKafkaTopics.PERFORMANCE_POINT_CALCULATED,
            key,
            event
        );
        log.info(
            "Published performance point event. employeeId={}, pointType={}, pointAmount={}, pointSourceId={}",
            event.getEmployeeId(),
            event.getPointType(),
            event.getPointAmount(),
            event.getPointSourceId()
        );
    }
}
