package com.ohgiraffers.team3backendbatch.batch.job.skillscore.processor;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.MonthlySkillContributionCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.TierAwareKpiScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MissionProgressEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.SkillGrowthCalculatedEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegratedScoreProcessor
    implements ItemProcessor<IntegratedScoreAggregate, IntegratedScoreAggregate> {

    private static final String POINT_SOURCE_TYPE = "EVALUATION_PERIOD_SETTLEMENT";
    private static final String QUANTITATIVE_POINT_TYPE = "QUANTITY";
    private static final String QUALITATIVE_POINT_TYPE = "QUALITATIVE";
    private static final String KNOWLEDGE_SHARING_POINT_TYPE = "KNOWLEDGE_SHARING";
    private static final String CHALLENGE_POINT_TYPE = "CHALLENGE";
    private static final String AI_SCORE_MISSION_TYPE = "AI_SCORE";

    private final PerformancePointCalculator performancePointCalculator;
    private final MonthlySkillContributionCalculator monthlySkillContributionCalculator;
    private final TierAwareKpiScoreCalculator tierAwareKpiScoreCalculator;

    @Override
    public IntegratedScoreAggregate process(IntegratedScoreAggregate item) {
        Integer quantitativePoint = null;
        Integer qualitativePoint = null;
        List<PerformancePointCalculatedEvent> events = new ArrayList<>();
        List<MissionProgressEvent> missionProgressEvents = new ArrayList<>();
        List<SkillGrowthCalculatedEvent> skillGrowthEvents = new ArrayList<>();
        boolean officialSettlement = item.getPeriodType() == BatchPeriodType.MONTH;

        BigDecimal quantitativeBaseScore = tierAwareKpiScoreCalculator.resolveQuantitativeSettlementScore(
            item.getEmployeeTier(),
            item.getQuantitativeTScore(),
            item.getQuantitativeProductivityScore(),
            item.getQuantitativeQualityScore(),
            item.getQuantitativeEquipmentResponseScore()
        );

        if (officialSettlement && quantitativeBaseScore != null) {
            quantitativePoint = applyEvaluationWeightToPoint(
                item,
                QUANTITATIVE_POINT_TYPE,
                performancePointCalculator.percentageToContributionPoint(quantitativeBaseScore)
            );
            events.add(buildEvent(
                item,
                QUANTITATIVE_POINT_TYPE,
                BigDecimal.valueOf(quantitativePoint),
                tierAwareKpiScoreCalculator.isStrategicTier(item.getEmployeeTier())
                    ? "Strategic-tier quantitative KPI settlement contribution"
                    : "Monthly quantitative settlement contribution"
            ));
        }

        BigDecimal capabilityScore = resolveCapabilityScore(quantitativeBaseScore, item.getQualitativeScore());
        if (officialSettlement && capabilityScore != null) {
            missionProgressEvents.add(MissionProgressEvent.builder()
                .employeeId(item.getEmployeeId())
                .missionType(AI_SCORE_MISSION_TYPE)
                .progressValue(capabilityScore)
                .absolute(true)
                .build());
        }

        if (officialSettlement && item.getQualitativeScore() != null) {
            qualitativePoint = applyEvaluationWeightToPoint(
                item,
                QUALITATIVE_POINT_TYPE,
                performancePointCalculator.percentageToContributionPoint(item.getQualitativeScore())
            );
            events.add(buildEvent(item, QUALITATIVE_POINT_TYPE, BigDecimal.valueOf(qualitativePoint), "Monthly qualitative settlement contribution"));
        }

        if (officialSettlement) {
            int kmsApprovedArticleCount = item.getKmsApprovedArticleCount() == null ? 0 : item.getKmsApprovedArticleCount();
            int challengeTaskCount = item.getChallengeTaskCount() == null ? 0 : item.getChallengeTaskCount();

            int kmsPoint = performancePointCalculator.kmsContributionPoint(kmsApprovedArticleCount);
            if (kmsPoint > 0) {
                kmsPoint = applyEvaluationWeightToPoint(item, KNOWLEDGE_SHARING_POINT_TYPE, kmsPoint);
                events.add(buildEvent(
                    item,
                    KNOWLEDGE_SHARING_POINT_TYPE,
                    BigDecimal.valueOf(kmsPoint),
                    "Monthly KMS approved article contribution"
                ));
            }

            int challengePoint = performancePointCalculator.challengeContributionPoint(challengeTaskCount);
            if (challengePoint > 0) {
                events.add(buildEvent(
                    item,
                    CHALLENGE_POINT_TYPE,
                    BigDecimal.valueOf(challengePoint),
                    "Monthly high-difficulty work contribution"
                ));
            }

            for (Map.Entry<String, BigDecimal> entry : monthlySkillContributionCalculator
                .calculateMonthlySkillContributions(
                    item.getEmployeeTier(),
                    item.getQuantitativeProductivityScore(),
                    item.getQuantitativeQualityScore(),
                    item.getQuantitativeEquipmentResponseScore(),
                    tierAwareKpiScoreCalculator.toKmsSignalScore(kmsApprovedArticleCount),
                    tierAwareKpiScoreCalculator.toChallengeSignalScore(challengeTaskCount),
                    item.getQualitativeSkillScores()
                )
                .entrySet()) {
                skillGrowthEvents.add(buildSkillGrowthEvent(item, entry.getKey(), entry.getValue()));
            }
        }

        return item.withCalculatedResults(quantitativePoint, qualitativePoint, missionProgressEvents, events, skillGrowthEvents);
    }

    private BigDecimal resolveCapabilityScore(BigDecimal quantitativeBaseScore, BigDecimal qualitativeScore) {
        if (quantitativeBaseScore == null) {
            return qualitativeScore;
        }
        if (qualitativeScore == null) {
            return quantitativeBaseScore;
        }
        return quantitativeBaseScore.add(qualitativeScore).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private PerformancePointCalculatedEvent buildEvent(
        IntegratedScoreAggregate item,
        String pointType,
        BigDecimal pointAmount,
        String description
    ) {
        return PerformancePointCalculatedEvent.builder()
            .employeeId(item.getEmployeeId())
            .evaluationPeriodId(item.getEvaluationPeriodId())
            .periodType(item.getPeriodType().name())
            .pointType(pointType)
            .pointAmount(pointAmount)
            .pointEarnedDate(item.getPointEarnedDate())
            .pointSourceId(item.getEvaluationPeriodId())
            .pointSourceType(POINT_SOURCE_TYPE)
            .pointDescription(description)
            .occurredAt(item.getOccurredAt())
            .build();
    }

    private int applyEvaluationWeightToPoint(
        IntegratedScoreAggregate item,
        String pointType,
        int basePoint
    ) {
        if (basePoint <= 0) {
            return basePoint;
        }

        String employeeTier = item.getEmployeeTier();
        Map<String, Integer> configuredWeights = item.getEvaluationCategoryWeights();

        return switch (pointType) {
            case QUANTITATIVE_POINT_TYPE -> scalePoint(
                basePoint,
                resolveCombinedConfiguredWeight(configuredWeights, employeeTier, "PRODUCTIVITY", "EQUIPMENT_RESPONSE"),
                resolveCombinedBaselineWeight(employeeTier, "PRODUCTIVITY", "EQUIPMENT_RESPONSE")
            );
            case QUALITATIVE_POINT_TYPE -> scalePoint(
                basePoint,
                resolveConfiguredWeight(configuredWeights, employeeTier, "PROCESS_INNOVATION"),
                resolveBaselineWeight(employeeTier, "PROCESS_INNOVATION")
            );
            case KNOWLEDGE_SHARING_POINT_TYPE -> scalePoint(
                basePoint,
                resolveConfiguredWeight(configuredWeights, employeeTier, "KNOWLEDGE_SHARING"),
                resolveBaselineWeight(employeeTier, "KNOWLEDGE_SHARING")
            );
            default -> basePoint;
        };
    }

    private int resolveCombinedConfiguredWeight(
        Map<String, Integer> configuredWeights,
        String employeeTier,
        String firstCategory,
        String secondCategory
    ) {
        return resolveConfiguredWeight(configuredWeights, employeeTier, firstCategory)
            + resolveConfiguredWeight(configuredWeights, employeeTier, secondCategory);
    }

    private int resolveCombinedBaselineWeight(String employeeTier, String firstCategory, String secondCategory) {
        return resolveBaselineWeight(employeeTier, firstCategory)
            + resolveBaselineWeight(employeeTier, secondCategory);
    }

    private int resolveConfiguredWeight(
        Map<String, Integer> configuredWeights,
        String employeeTier,
        String category
    ) {
        if (configuredWeights == null || configuredWeights.isEmpty()) {
            return resolveBaselineWeight(employeeTier, category);
        }
        return configuredWeights.getOrDefault(category, resolveBaselineWeight(employeeTier, category));
    }

    private int resolveBaselineWeight(String employeeTier, String category) {
        boolean strategicTier = tierAwareKpiScoreCalculator.isStrategicTier(employeeTier);
        return switch (category) {
            case "PRODUCTIVITY" -> strategicTier ? 20 : 60;
            case "EQUIPMENT_RESPONSE" -> strategicTier ? 40 : 20;
            case "PROCESS_INNOVATION" -> strategicTier ? 30 : 10;
            case "KNOWLEDGE_SHARING" -> 10;
            default -> 100;
        };
    }

    private int scalePoint(int basePoint, int configuredWeight, int baselineWeight) {
        if (baselineWeight <= 0) {
            return basePoint;
        }
        return BigDecimal.valueOf(basePoint)
            .multiply(BigDecimal.valueOf(configuredWeight))
            .divide(BigDecimal.valueOf(baselineWeight), 0, RoundingMode.HALF_UP)
            .intValue();
    }

    private SkillGrowthCalculatedEvent buildSkillGrowthEvent(
        IntegratedScoreAggregate item,
        String skillCategory,
        BigDecimal contributionScore
    ) {
        return SkillGrowthCalculatedEvent.builder()
            .employeeId(item.getEmployeeId())
            .evaluationPeriodId(item.getEvaluationPeriodId())
            .periodType(item.getPeriodType().name())
            .skillCategory(skillCategory)
            .skillContributionScore(contributionScore)
            .alpha(monthlySkillContributionCalculator.getDefaultAlpha())
            .contributionDate(item.getPointEarnedDate())
            .sourceId(item.getEvaluationPeriodId())
            .sourceType(POINT_SOURCE_TYPE)
            .occurredAt(item.getOccurredAt())
            .build();
    }
}
