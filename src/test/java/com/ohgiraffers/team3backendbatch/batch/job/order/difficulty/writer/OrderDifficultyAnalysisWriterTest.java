package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.writer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultyResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.OrderDifficultyAnalysisEventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class OrderDifficultyAnalysisWriterTest {

    @Mock
    private OrderDifficultyAnalysisEventPublisher orderDifficultyAnalysisEventPublisher;

    @InjectMocks
    private OrderDifficultyAnalysisWriter orderDifficultyAnalysisWriter;

    @Test
    @DisplayName("Publishes analyzed event for each processed order")
    void write_PublishesAnalyzedEvent() throws Exception {
        OrderDifficultyResult result = OrderDifficultyResult.builder()
            .orderId(101L)
            .v1ProcessComplexity(new BigDecimal("8.20"))
            .v2QualityPrecision(new BigDecimal("9.10"))
            .v3CapacityRequirements(new BigDecimal("7.40"))
            .v4SpaceTimeUrgency(new BigDecimal("9.50"))
            .alphaNovelty(new BigDecimal("10.00"))
            .difficultyScore(new BigDecimal("92.40"))
            .difficultyGrade("D5")
            .analysisStatus("ANALYZED")
            .analyzedAt(LocalDateTime.now())
            .build();

        orderDifficultyAnalysisWriter.write(new Chunk<>(result));

        verify(orderDifficultyAnalysisEventPublisher).publishAnalyzed(any());
    }
}
