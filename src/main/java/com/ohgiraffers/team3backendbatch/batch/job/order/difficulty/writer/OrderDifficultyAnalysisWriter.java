package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.writer;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultyResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderDifficultyAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.OrderDifficultyAnalysisEventPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class OrderDifficultyAnalysisWriter implements ItemWriter<OrderDifficultyResult> {

    private static final Logger log = LoggerFactory.getLogger(OrderDifficultyAnalysisWriter.class);

    private final OrderDifficultyAnalysisEventPublisher orderDifficultyAnalysisEventPublisher;

    @Override
    public void write(Chunk<? extends OrderDifficultyResult> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        List<? extends OrderDifficultyResult> results = List.copyOf(chunk.getItems());
        publishAnalyzedEventsAfterCommit(results);
        log.info("Queued order difficulty analyzed events. itemCount={}", results.size());
    }

    private void publishAnalyzedEventsAfterCommit(List<? extends OrderDifficultyResult> results) {
        Runnable publishAction = () -> results.forEach(result ->
            orderDifficultyAnalysisEventPublisher.publishAnalyzed(
                new OrderDifficultyAnalyzedEvent(
                    result.getOrderId(),
                    result.getV1ProcessComplexity(),
                    result.getV2QualityPrecision(),
                    result.getV3CapacityRequirements(),
                    result.getV4SpaceTimeUrgency(),
                    result.getAlphaNovelty(),
                    result.getDifficultyScore(),
                    result.getDifficultyGrade(),
                    result.getAnalysisStatus(),
                    result.getAnalyzedAt()
                )
            )
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            return;
        }

        publishAction.run();
    }
}
