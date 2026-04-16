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

    /**
     * 주문 난이도 분석 결과 이벤트를 발행한다.
     * @param chunk 발행할 주문 난이도 분석 결과 묶음
     * @return 반환값 없음
     */
    @Override
    public void write(Chunk<? extends OrderDifficultyResult> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        List<? extends OrderDifficultyResult> results = List.copyOf(chunk.getItems());
        publishAnalyzedEventsAfterCommit(results);
        log.info("Queued order difficulty analyzed events. itemCount={}", results.size());
    }

    /**
     * 트랜잭션 커밋 이후 주문 난이도 분석 이벤트를 발행한다.
     * @param results 발행할 주문 난이도 분석 결과 목록
     * @return 반환값 없음
     */
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
