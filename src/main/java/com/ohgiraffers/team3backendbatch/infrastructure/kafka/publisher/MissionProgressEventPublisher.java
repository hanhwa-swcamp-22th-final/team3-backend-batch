package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MissionProgressEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MissionKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionProgressEventPublisher {

    private final KafkaTemplate<String, MissionProgressEvent> missionProgressKafkaTemplate;

    public void publish(MissionProgressEvent event) {
        missionProgressKafkaTemplate.send(
                MissionKafkaTopics.MISSION_PROGRESS_UPDATED,
                String.valueOf(event.getEmployeeId()),
                event
        );
        log.info(
                "[Mission] Published mission progress event. employeeId={}, type={}, value={}, absolute={}",
                event.getEmployeeId(),
                event.getMissionType(),
                event.getProgressValue(),
                event.isAbsolute()
        );
    }
}
