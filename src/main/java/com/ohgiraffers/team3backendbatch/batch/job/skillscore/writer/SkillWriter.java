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

    /**
     * 스킬 성장 계산 이벤트를 발행한다.
     * @param chunk 스킬 성장 이벤트를 담고 있는 집계 결과 청크
     * @return 반환값 없음
     */
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
