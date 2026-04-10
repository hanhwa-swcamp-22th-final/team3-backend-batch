package com.ohgiraffers.team3backendbatch.batch.job.score.writer;

import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.PerformancePointEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformancePointWriter implements ItemWriter<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(PerformancePointWriter.class);

    private final PerformancePointEventPublisher performancePointEventPublisher;

    @Override
    public void write(Chunk<? extends IntegratedScoreAggregate> chunk) {
        int eventCount = 0;
        for (IntegratedScoreAggregate item : chunk.getItems()) {
            for (PerformancePointCalculatedEvent event : item.getPerformancePointEvents()) {
                performancePointEventPublisher.publishCalculated(event);
                eventCount++;
            }
        }
        log.info("Published performance point events. itemCount={}, eventCount={}", chunk.size(), eventCount);
    }
}
