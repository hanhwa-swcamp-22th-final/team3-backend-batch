package com.ohgiraffers.team3backendbatch.batch.job.skillscore.writer;

import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.SkillGrowthCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.SkillGrowthEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillWriter implements ItemWriter<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(SkillWriter.class);
    private final SkillGrowthEventPublisher skillGrowthEventPublisher;

    @Override
    public void write(Chunk<? extends IntegratedScoreAggregate> chunk) {
        int eventCount = 0;
        for (IntegratedScoreAggregate item : chunk.getItems()) {
            for (SkillGrowthCalculatedEvent event : item.getSkillGrowthEvents()) {
                skillGrowthEventPublisher.publishCalculated(event);
                eventCount++;
            }
        }
        log.info("Published skill growth events. itemCount={}, eventCount={}", chunk.size(), eventCount);
    }
}
