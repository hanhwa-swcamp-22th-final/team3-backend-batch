package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.processor;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultyResult;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import com.ohgiraffers.team3backendbatch.domain.order.scoring.OrderDifficultyCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDifficultyProcessor implements ItemProcessor<OrderDifficultySource, OrderDifficultyResult> {

    private final OrderDifficultyCalculator orderDifficultyCalculator;

    @Override
    public OrderDifficultyResult process(OrderDifficultySource item) {
        if (item == null) {
            return null;
        }

        BigDecimal v1 = orderDifficultyCalculator.calculateProcessComplexity(item);
        BigDecimal v2 = orderDifficultyCalculator.calculateQualityPrecision(item);
        BigDecimal v3CompetencyRequirements = orderDifficultyCalculator.calculateCompetencyRequirements(item);
        BigDecimal v4 = orderDifficultyCalculator.calculateSpaceTimeUrgency(item);
        BigDecimal alphaNovelty = orderDifficultyCalculator.calculateAlphaNovelty(item);
        BigDecimal difficultyScore = orderDifficultyCalculator.calculateDifficultyScore(
            item,
            v1,
            v2,
            v3CompetencyRequirements,
            v4,
            alphaNovelty
        );
        String difficultyGrade = orderDifficultyCalculator.classifyDifficultyGrade(difficultyScore);

        return OrderDifficultyResult.builder()
            .orderId(item.getOrderId())
            .v1ProcessComplexity(v1)
            .v2QualityPrecision(v2)
            .v3CapacityRequirements(v3CompetencyRequirements)
            .v4SpaceTimeUrgency(v4)
            .alphaNovelty(alphaNovelty)
            .difficultyScore(difficultyScore)
            .difficultyGrade(difficultyGrade)
            .analysisStatus("ANALYZED")
            .analyzedAt(LocalDateTime.now())
            .build();
    }
}
