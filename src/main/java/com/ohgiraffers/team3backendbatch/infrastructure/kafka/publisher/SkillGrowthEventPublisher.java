package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.SkillGrowthCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.SkillGrowthKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillGrowthEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SkillGrowthEventPublisher.class);

    private final KafkaTemplate<String, SkillGrowthCalculatedEvent> skillGrowthCalculatedKafkaTemplate;

    public void publishCalculated(SkillGrowthCalculatedEvent event) {
        String key = event.getEmployeeId() + ":" + event.getSkillCategory() + ":" + event.getSourceId();
        skillGrowthCalculatedKafkaTemplate.send(
            SkillGrowthKafkaTopics.SKILL_GROWTH_CALCULATED,
            key,
            event
        );
        log.info(
            "Published skill growth event. employeeId={}, skillCategory={}, contributionScore={}, sourceId={}",
            event.getEmployeeId(),
            event.getSkillCategory(),
            event.getSkillContributionScore(),
            event.getSourceId()
        );
    }
}
