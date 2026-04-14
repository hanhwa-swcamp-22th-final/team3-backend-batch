package com.ohgiraffers.team3backendbatch.batch.job.skillscore.writer;

import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MissionProgressEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.MissionProgressEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionProgressWriter implements ItemWriter<IntegratedScoreAggregate> {

    private final MissionProgressEventPublisher missionProgressEventPublisher;

    @Override
    public void write(Chunk<? extends IntegratedScoreAggregate> chunk) {
        int eventCount = 0;
        for (IntegratedScoreAggregate item : chunk.getItems()) {
            for (MissionProgressEvent event : item.getMissionProgressEvents()) {
                missionProgressEventPublisher.publish(event);
                eventCount++;
            }
        }
        log.info("Published mission progress events. itemCount={}, eventCount={}", chunk.size(), eventCount);
    }
}
