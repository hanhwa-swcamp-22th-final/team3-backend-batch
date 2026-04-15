package com.ohgiraffers.team3backendbatch.batch.job.skillscore.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.MatchedKeywordDetail;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeSkillKeywordClassifier;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MatchedKeywordDetailEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.mapper.KmsApprovedArticleCountRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.mapper.KmsArticleQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.mapper.EmployeeChallengeCountRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.mapper.OrderAssignmentQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.EvaluationWeightConfigProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.EvaluationWeightConfigProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.EvaluationCommentQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.MonthlyMatchedKeywordRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.MonthlyQualitativeScoreRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeScoreQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodProjectionRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.MonthlyQuantitativeScoreRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.QuantitativeEvaluationAggregateQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class IntegratedScoreReader implements ItemReader<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(IntegratedScoreReader.class);

    private final EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;
    private final EvaluationPeriodQueryMapper evaluationPeriodQueryMapper;
    private final EmployeeProjectionRepository employeeProjectionRepository;
    private final QuantitativeEvaluationAggregateQueryMapper quantitativeEvaluationAggregateQueryMapper;
    private final QualitativeScoreQueryMapper qualitativeScoreQueryMapper;
    private final EvaluationCommentQueryMapper evaluationCommentQueryMapper;
    private final OrderAssignmentQueryMapper orderAssignmentQueryMapper;
    private final KmsArticleQueryMapper kmsArticleQueryMapper;
    private final EvaluationWeightConfigProjectionRepository evaluationWeightConfigProjectionRepository;
    private final QualitativeSkillKeywordClassifier qualitativeSkillKeywordClassifier;
    private final ObjectMapper objectMapper;

    @Value("#{jobParameters['evaluationPeriodId']}")
    private Long requestedEvaluationPeriodId;

    @Value("#{jobParameters['periodType']}")
    private String requestedPeriodType;

    private Iterator<IntegratedScoreAggregate> iterator = Collections.emptyIterator();
    private boolean initialized;

    @Override
    public IntegratedScoreAggregate read() {
        if (!initialized) {
            initialized = true;
            iterator = loadItems().iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<IntegratedScoreAggregate> loadItems() {
        BatchPeriodType periodType = parsePeriodType(requestedPeriodType);
        if (periodType != BatchPeriodType.MONTH) {
            log.info("Skipping score aggregation because only monthly settlement is supported. periodType={}", periodType);
            return List.of();
        }

        EvaluationPeriodProjectionRow evaluationPeriod = resolveEvaluationPeriod(periodType).orElse(null);

        if (evaluationPeriod == null) {
            log.info("Skipping score aggregation. No confirmed evaluation period was found. periodType={}, requestedEvaluationPeriodId={}", periodType, requestedEvaluationPeriodId);
            return List.of();
        }

        Long evaluationPeriodId = evaluationPeriod.getEvaluationPeriodId();
        LocalDate startDate = evaluationPeriod.getStartDate();
        LocalDate endDate = evaluationPeriod.getEndDate();
        LocalDate pointEarnedDate = endDate;
        LocalDateTime occurredAt = LocalDateTime.now();

        Map<Long, MonthlyQuantitativeScoreRow> quantitativeScores = loadQuantitativeScores(
            evaluationPeriodId
        );
        Map<Long, BigDecimal> qualitativeScores = loadQualitativeScores(evaluationPeriodId);
        Map<Long, Map<String, BigDecimal>> qualitativeSkillScores = buildQualitativeSkillScores(evaluationPeriodId, qualitativeScores);
        Map<String, Map<String, Integer>> evaluationCategoryWeightsByTierGroup = loadEvaluationCategoryWeights();
        Map<Long, Integer> kmsApprovedArticleCounts = loadKmsApprovedArticleCounts(startDate, endDate);
        Map<Long, Integer> challengeCounts = orderAssignmentQueryMapper.findChallengeCountsByAssignedAtBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
            .stream()
            .collect(Collectors.toMap(
                EmployeeChallengeCountRow::getEmployeeId,
                view -> view.getChallengeCount() == null ? 0 : view.getChallengeCount()
            ));

        List<IntegratedScoreAggregate> items = employeeProjectionRepository.findAll().stream()
            .filter(this::isEligibleEmployee)
            .map(employee -> buildAggregate(
                employee,
                evaluationPeriodId,
                periodType,
                pointEarnedDate,
                occurredAt,
                quantitativeScores,
                qualitativeScores,
                qualitativeSkillScores,
                evaluationCategoryWeightsByTierGroup,
                kmsApprovedArticleCounts,
                challengeCounts
            ))
            .filter(Objects::nonNull)
            .toList();

        log.info(
            "Prepared integrated score targets. evaluationPeriodId={}, periodType={}, employeeCount={}, quantitativeCount={}, qualitativeCount={}, skillSignalCount={}, kmsCount={}, challengeCount={}",
            evaluationPeriodId,
            periodType,
            items.size(),
            quantitativeScores.size(),
            qualitativeScores.size(),
            qualitativeSkillScores.size(),
            kmsApprovedArticleCounts.size(),
            challengeCounts.size()
        );
        return items;
    }

    private Optional<EvaluationPeriodProjectionRow> resolveEvaluationPeriod(BatchPeriodType periodType) {
        if (requestedEvaluationPeriodId != null) {
            return evaluationPeriodProjectionRepository.findById(requestedEvaluationPeriodId)
                .map(this::toPeriodRow)
                .map(this::validateEvaluationPeriod);
        }
        return evaluationPeriodQueryMapper.findLatestConfirmedPeriod(periodType);
    }

    private EvaluationPeriodProjectionRow validateEvaluationPeriod(EvaluationPeriodProjectionRow period) {
        if (!"CONFIRMED".equalsIgnoreCase(period.getStatus())) {
            throw new IllegalStateException(
                "Requested evaluation period is not confirmed. evaluationPeriodId="
                    + period.getEvaluationPeriodId()
                    + ", status="
                    + period.getStatus()
            );
        }
        return period;
    }

    private Map<Long, MonthlyQuantitativeScoreRow> loadQuantitativeScores(
        Long evaluationPeriodId
    ) {
        List<MonthlyQuantitativeScoreRow> values =
            quantitativeEvaluationAggregateQueryMapper.findAverageScoresByEvaluationPeriodIdAndStatusIn(
                evaluationPeriodId,
                resolveQuantitativeStatuses()
            );

        return values.stream().collect(Collectors.toMap(
            MonthlyQuantitativeScoreRow::getEmployeeId,
            view -> view
        ));
    }

    private Map<Long, BigDecimal> loadQualitativeScores(Long evaluationPeriodId) {
        List<MonthlyQualitativeScoreRow> values =
            qualitativeScoreQueryMapper.findLatestNormalizedScoresByEvaluationPeriodId(evaluationPeriodId);

        return values.stream().collect(Collectors.toMap(
            MonthlyQualitativeScoreRow::getEmployeeId,
            MonthlyQualitativeScoreRow::getNormalizedScore
        ));
    }

    private Map<Long, Integer> loadKmsApprovedArticleCounts(LocalDate startDate, LocalDate endDate) {
        return kmsArticleQueryMapper.findApprovedArticleCountsByApprovedAtBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
            .stream()
            .collect(Collectors.toMap(
                KmsApprovedArticleCountRow::getEmployeeId,
                view -> view.getApprovedArticleCount() == null ? 0 : view.getApprovedArticleCount().intValue()
            ));
    }

    private Map<Long, Map<String, BigDecimal>> buildQualitativeSkillScores(
        Long evaluationPeriodId,
        Map<Long, BigDecimal> qualitativeScores
    ) {
        Map<Long, List<MatchedKeywordDetail>> matchedKeywordDetailsByEmployee = new LinkedHashMap<>();

        List<MonthlyMatchedKeywordRow> keywordViews =
            evaluationCommentQueryMapper.findMatchedKeywordsByEvaluationPeriodId(evaluationPeriodId);

        for (MonthlyMatchedKeywordRow view : keywordViews) {
            if (view.getEmployeeId() == null) {
                continue;
            }
            matchedKeywordDetailsByEmployee
                .computeIfAbsent(view.getEmployeeId(), ignored -> new ArrayList<>())
                .addAll(parseMatchedKeywordDetails(view.getMatchedKeywordDetails(), view.getMatchedKeywords()));
        }

        Map<Long, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : qualitativeScores.entrySet()) {
            Map<String, BigDecimal> skillScores = qualitativeSkillKeywordClassifier.toSkillScores(
                entry.getValue(),
                matchedKeywordDetailsByEmployee.getOrDefault(entry.getKey(), List.of())
            );
            if (!skillScores.isEmpty()) {
                result.put(entry.getKey(), skillScores);
            }
        }
        return result;
    }

    private IntegratedScoreAggregate buildAggregate(
        EmployeeProjectionEntity employee,
        Long evaluationPeriodId,
        BatchPeriodType periodType,
        LocalDate pointEarnedDate,
        LocalDateTime occurredAt,
        Map<Long, MonthlyQuantitativeScoreRow> quantitativeScores,
        Map<Long, BigDecimal> qualitativeScores,
        Map<Long, Map<String, BigDecimal>> qualitativeSkillScores,
        Map<String, Map<String, Integer>> evaluationCategoryWeightsByTierGroup,
        Map<Long, Integer> kmsApprovedArticleCounts,
        Map<Long, Integer> challengeCounts
    ) {
        MonthlyQuantitativeScoreRow quantitativeView = quantitativeScores.get(employee.getEmployeeId());
        BigDecimal quantitativeTScore = quantitativeView == null ? null : quantitativeView.getAverageTScore();
        BigDecimal quantitativeProductivityScore = quantitativeView == null ? null : quantitativeView.getAverageProductivityScore();
        BigDecimal quantitativeQualityScore = quantitativeView == null ? null : quantitativeView.getAverageQualityScore();
        BigDecimal quantitativeEquipmentResponseScore = quantitativeView == null ? null : quantitativeView.getAverageEquipmentResponseScore();
        BigDecimal qualitativeScore = qualitativeScores.get(employee.getEmployeeId());
        Map<String, BigDecimal> employeeQualitativeSkillScores = qualitativeSkillScores.getOrDefault(employee.getEmployeeId(), Map.of());
        Map<String, Integer> evaluationCategoryWeights = evaluationCategoryWeightsByTierGroup.getOrDefault(
            resolveEvaluationTierGroup(employee.getEmployeeTier()),
            Map.of()
        );
        Integer kmsApprovedArticleCount = kmsApprovedArticleCounts.getOrDefault(employee.getEmployeeId(), 0);
        Integer challengeTaskCount = challengeCounts.getOrDefault(employee.getEmployeeId(), 0);

        if (quantitativeTScore == null
            && qualitativeScore == null
            && employeeQualitativeSkillScores.isEmpty()
            && kmsApprovedArticleCount <= 0
            && challengeTaskCount <= 0) {
            return null;
        }

        return IntegratedScoreAggregate.builder()
            .employeeId(employee.getEmployeeId())
            .employeeTier(employee.getEmployeeTier())
            .evaluationPeriodId(evaluationPeriodId)
            .periodType(periodType)
            .pointEarnedDate(pointEarnedDate)
            .occurredAt(occurredAt)
            .quantitativeTScore(quantitativeTScore)
            .quantitativeProductivityScore(quantitativeProductivityScore)
            .quantitativeQualityScore(quantitativeQualityScore)
            .quantitativeEquipmentResponseScore(quantitativeEquipmentResponseScore)
            .qualitativeScore(qualitativeScore)
            .qualitativeSkillScores(employeeQualitativeSkillScores)
            .evaluationCategoryWeights(evaluationCategoryWeights)
            .kmsApprovedArticleCount(kmsApprovedArticleCount)
            .challengeTaskCount(challengeTaskCount)
            .missionProgressEvents(List.of())
            .performancePointEvents(List.of())
            .build();
    }

    private Map<String, Map<String, Integer>> loadEvaluationCategoryWeights() {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        for (EvaluationWeightConfigProjectionEntity projection : evaluationWeightConfigProjectionRepository.findAllByActiveTrueAndDeletedFalse()) {
            if (projection.getTierGroup() == null || projection.getCategoryCode() == null || projection.getWeightPercent() == null) {
                continue;
            }
            result.computeIfAbsent(projection.getTierGroup().trim().toUpperCase(), ignored -> new LinkedHashMap<>())
                .put(projection.getCategoryCode().trim().toUpperCase(), projection.getWeightPercent());
        }
        return result;
    }

    private String resolveEvaluationTierGroup(String employeeTier) {
        if (employeeTier == null || employeeTier.isBlank()) {
            return "BC";
        }
        String normalized = employeeTier.trim().toUpperCase();
        return ("S".equals(normalized) || "A".equals(normalized)) ? "SA" : "BC";
    }

    private List<MatchedKeywordDetail> parseMatchedKeywordDetails(String detailsJson, String keywordsJson) {
        try {
            if (detailsJson != null && !detailsJson.isBlank()) {
                List<MatchedKeywordDetailEvent> values = objectMapper.readValue(
                    detailsJson,
                    new TypeReference<List<MatchedKeywordDetailEvent>>() { }
                );
                if (values != null && !values.isEmpty()) {
                    return values.stream()
                        .map(value -> new MatchedKeywordDetail(
                            value.getDomainKeywordId(),
                            value.getKeyword(),
                            value.getDomainCompetencyCategory(),
                            value.getScoreWeight()
                        ))
                        .toList();
                }
            }

            if (keywordsJson == null || keywordsJson.isBlank()) {
                return List.of();
            }

            List<String> values = objectMapper.readValue(keywordsJson, new TypeReference<List<String>>() { });
            if (values == null) {
                return List.of();
            }
            return values.stream()
                .map(value -> new MatchedKeywordDetail(null, value, null, null))
                .toList();
        } catch (Exception exception) {
            log.warn(
                "Failed to parse matched keyword details JSON. detailsPayload={}, keywordsPayload= {}",
                detailsJson,
                keywordsJson,
                exception
            );
            return List.of();
        }
    }

    private Collection<String> resolveQuantitativeStatuses() {
        return List.of("CONFIRMED");
    }

    private EvaluationPeriodProjectionRow toPeriodRow(EvaluationPeriodProjectionEntity entity) {
        EvaluationPeriodProjectionRow row = new EvaluationPeriodProjectionRow();
        row.setEvaluationPeriodId(entity.getEvaluationPeriodId());
        row.setAlgorithmVersionId(entity.getAlgorithmVersionId());
        row.setEvaluationYear(entity.getEvaluationYear());
        row.setEvaluationSequence(entity.getEvaluationSequence());
        row.setStartDate(entity.getStartDate());
        row.setEndDate(entity.getEndDate());
        row.setStatus(entity.getStatus());
        row.setPolicyConfig(entity.getPolicyConfig());
        row.setParameters(entity.getParameters());
        row.setReferenceValues(entity.getReferenceValues());
        return row;
    }

    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.MONTH;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    private boolean isEligibleEmployee(EmployeeProjectionEntity employee) {
        if (employee.getEmployeeId() == null) {
            return false;
        }
        return employee.getEmployeeStatus() == null || !"INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
    }
}
