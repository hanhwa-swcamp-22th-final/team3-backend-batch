package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QuantitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeEvaluationEventPublisher.class);

    private final KafkaTemplate<String, QuantitativeEvaluationCalculatedEvent> quantitativeCalculatedKafkaTemplate;

    public void publishCalculated(QuantitativeEvaluationCalculatedEvent event) {
        String key = event.getEmployeeId() + ":" + event.getEvaluationPeriodId();
        quantitativeCalculatedKafkaTemplate.send(
            QuantitativeKafkaTopics.QUANTITATIVE_EVALUATION_CALCULATED,
            key,
            event
        );
        log.info(
            "Published quantitative calculated event. employeeId={}, evaluationPeriodId={}, equipmentCount={}, periodType={}",
            event.getEmployeeId(),
            event.getEvaluationPeriodId(),
            event.getEquipmentResults() == null ? 0 : event.getEquipmentResults().size(),
            event.getPeriodType()
        );
    }
}
