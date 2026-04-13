package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EquipmentBaselineCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.EquipmentBaselineKafkaTopics;
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
    private final KafkaTemplate<String, EquipmentBaselineCalculatedEvent> equipmentBaselineCalculatedKafkaTemplate;

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

    public void publishEquipmentBaselineCalculated(EquipmentBaselineCalculatedEvent event) {
        String key = String.valueOf(event.getEquipmentId());
        equipmentBaselineCalculatedKafkaTemplate.send(
            EquipmentBaselineKafkaTopics.EQUIPMENT_BASELINE_CALCULATED,
            key,
            event
        ).join();
        log.info(
            "Published equipment baseline calculated event. equipmentId={}, equipmentIdx={}, currentEquipmentGrade={}, periodType={}",
            event.getEquipmentId(),
            event.getEquipmentIdx(),
            event.getCurrentEquipmentGrade(),
            event.getPeriodType()
        );
    }
}
