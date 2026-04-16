package com.ohgiraffers.team3backendbatch.batch.job.skillscore.processor;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.scoring.MonthlySkillContributionCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.PerformancePointCalculator;
import com.ohgiraffers.team3backendbatch.domain.scoring.TierAwareKpiScoreCalculator;
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

    private final PerformancePointCalculator performancePointCalculator;
    private final MonthlySkillContributionCalculator monthlySkillContributionCalculator;
    private final TierAwareKpiScoreCalculator tierAwareKpiScoreCalculator;

    /**
     * 월간 정량·정성·부가 지표를 종합해 성과 포인트와 스킬 성장 이벤트를 계산한다.
     * @param item 종합 점수 집계 원본 데이터
     * @return 계산 결과가 반영된 종합 점수 집계 데이터
     */
    @Override
    public IntegratedScoreAggregate process(IntegratedScoreAggregate item) {
        Integer quantitativePoint = null;
        Integer qualitativePoint = null;
        List<PerformancePointCalculatedEvent> events = new ArrayList<>();
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
                    : "Monthly quantitative settlement contribution",
                item.getQualitativeScore() == null
                    ? resolveCapabilityScore(quantitativeBaseScore, item.getQualitativeScore())
                    : null
            ));
        }

        BigDecimal capabilityScore = resolveCapabilityScore(quantitativeBaseScore, item.getQualitativeScore());
        if (officialSettlement && item.getQualitativeScore() != null) {
            qualitativePoint = applyEvaluationWeightToPoint(
                item,
                QUALITATIVE_POINT_TYPE,
                performancePointCalculator.percentageToContributionPoint(item.getQualitativeScore())
            );
            events.add(buildEvent(
                item,
                QUALITATIVE_POINT_TYPE,
                BigDecimal.valueOf(qualitativePoint),
                "Monthly qualitative settlement contribution",
                capabilityScore
            ));
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
                    "Monthly KMS approved article contribution",
                    null
                ));
            }

            int challengePoint = performancePointCalculator.challengeContributionPoint(challengeTaskCount);
            if (challengePoint > 0) {
                events.add(buildEvent(
                    item,
                    CHALLENGE_POINT_TYPE,
                    BigDecimal.valueOf(challengePoint),
                    "Monthly high-difficulty work contribution",
                    null
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

        return item.withCalculatedResults(quantitativePoint, qualitativePoint, events, skillGrowthEvents);
    }

    /**
     * 정량 점수와 정성 점수를 이용해 capability score 를 계산한다.
     * @param quantitativeBaseScore 정량 기준 점수
     * @param qualitativeScore 정성 점수
     * @return capability score
     */
    private BigDecimal resolveCapabilityScore(BigDecimal quantitativeBaseScore, BigDecimal qualitativeScore) {
        if (quantitativeBaseScore == null) {
            return qualitativeScore;
        }
        if (qualitativeScore == null) {
            return quantitativeBaseScore;
        }
        return quantitativeBaseScore.add(qualitativeScore).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    /**
     * 성과 포인트 계산 이벤트를 생성한다.
     * @param item 종합 점수 집계 데이터
     * @param pointType 포인트 유형
     * @param pointAmount 포인트 값
     * @param description 포인트 설명
     * @param capabilityScore capability score
     * @return 성과 포인트 계산 이벤트
     */
    private PerformancePointCalculatedEvent buildEvent(
        IntegratedScoreAggregate item,
        String pointType,
        BigDecimal pointAmount,
        String description,
        BigDecimal capabilityScore
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
            .capabilityScore(capabilityScore)
            .occurredAt(item.getOccurredAt())
            .build();
    }

    /**
     * 평가 비중을 반영해 포인트를 보정한다.
     * @param item 종합 점수 집계 데이터
     * @param pointType 포인트 유형
     * @param basePoint 기본 포인트
     * @return 평가 비중이 반영된 포인트
     */
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

    /**
     * 두 카테고리의 설정 비중 합계를 계산한다.
     * @param configuredWeights tier 그룹별 설정 비중 맵
     * @param employeeTier 직원 tier
     * @param firstCategory 첫 번째 카테고리
     * @param secondCategory 두 번째 카테고리
     * @return 설정 비중 합계
     */
    private int resolveCombinedConfiguredWeight(
        Map<String, Integer> configuredWeights,
        String employeeTier,
        String firstCategory,
        String secondCategory
    ) {
        return resolveConfiguredWeight(configuredWeights, employeeTier, firstCategory)
            + resolveConfiguredWeight(configuredWeights, employeeTier, secondCategory);
    }

    /**
     * 두 카테고리의 기본 비중 합계를 계산한다.
     * @param employeeTier 직원 tier
     * @param firstCategory 첫 번째 카테고리
     * @param secondCategory 두 번째 카테고리
     * @return 기본 비중 합계
     */
    private int resolveCombinedBaselineWeight(String employeeTier, String firstCategory, String secondCategory) {
        return resolveBaselineWeight(employeeTier, firstCategory)
            + resolveBaselineWeight(employeeTier, secondCategory);
    }

    /**
     * 카테고리별 설정 비중을 조회한다.
     * @param configuredWeights tier 그룹별 설정 비중 맵
     * @param employeeTier 직원 tier
     * @param category 카테고리 코드
     * @return 설정 비중 또는 기본 비중
     */
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

    /**
     * 카테고리별 기본 비중을 계산한다.
     * @param employeeTier 직원 tier
     * @param category 카테고리 코드
     * @return 기본 비중
     */
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

    /**
     * 설정 비중과 기본 비중을 비교해 포인트를 스케일링한다.
     * @param basePoint 기본 포인트
     * @param configuredWeight 설정 비중
     * @param baselineWeight 기본 비중
     * @return 스케일링된 포인트
     */
    private int scalePoint(int basePoint, int configuredWeight, int baselineWeight) {
        if (baselineWeight <= 0) {
            return basePoint;
        }
        return BigDecimal.valueOf(basePoint)
            .multiply(BigDecimal.valueOf(configuredWeight))
            .divide(BigDecimal.valueOf(baselineWeight), 0, RoundingMode.HALF_UP)
            .intValue();
    }

    /**
     * 스킬 성장 계산 이벤트를 생성한다.
     * @param item 종합 점수 집계 데이터
     * @param skillCategory 스킬 카테고리
     * @param contributionScore 스킬 기여 점수
     * @return 스킬 성장 계산 이벤트
     */
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
