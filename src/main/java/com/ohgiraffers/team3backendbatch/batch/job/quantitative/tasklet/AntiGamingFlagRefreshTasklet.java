package com.ohgiraffers.team3backendbatch.batch.job.quantitative.tasklet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.MatchedKeywordDetail;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeSkillKeywordClassifier;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MatchedKeywordDetailEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.EvaluationCommentQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.MonthlyMatchedKeywordRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.AntiGamingFlagEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.AntiGamingProductionScoreRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.AntiGamingSourceQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.AntiGamingFlagRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class AntiGamingFlagRefreshTasklet implements Tasklet {

    private static final BigDecimal DEFAULT_PENALTY = new BigDecimal("5.00");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final double OUTLIER_RATIO = 0.20d;
    private static final String SAFETY_CATEGORY = "SAFETY_COMPLIANCE";

    private final EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;
    private final AntiGamingSourceQueryMapper antiGamingSourceQueryMapper;
    private final EvaluationCommentQueryMapper evaluationCommentQueryMapper;
    private final AntiGamingFlagRepository antiGamingFlagRepository;
    private final QualitativeSkillKeywordClassifier qualitativeSkillKeywordClassifier;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final Long requestedEvaluationPeriodId;
    private final String requestedPeriodType;

    public AntiGamingFlagRefreshTasklet(
        EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository,
        AntiGamingSourceQueryMapper antiGamingSourceQueryMapper,
        EvaluationCommentQueryMapper evaluationCommentQueryMapper,
        AntiGamingFlagRepository antiGamingFlagRepository,
        QualitativeSkillKeywordClassifier qualitativeSkillKeywordClassifier,
        ObjectMapper objectMapper,
        IdGenerator idGenerator,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['periodType']}") String periodType
    ) {
        this.evaluationPeriodProjectionRepository = evaluationPeriodProjectionRepository;
        this.antiGamingSourceQueryMapper = antiGamingSourceQueryMapper;
        this.evaluationCommentQueryMapper = evaluationCommentQueryMapper;
        this.antiGamingFlagRepository = antiGamingFlagRepository;
        this.qualitativeSkillKeywordClassifier = qualitativeSkillKeywordClassifier;
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
        this.requestedEvaluationPeriodId = evaluationPeriodId;
        this.requestedPeriodType = periodType;
    }

    /**
     * 월간 anti-gaming flag 를 산출해 저장한다.
     * @param contribution step 기여 정보
     * @param chunkContext chunk 문맥
     * @return tasklet 반복 종료 상태
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        EvaluationPeriodProjectionEntity evaluationPeriod = resolveMonthlyEvaluationPeriod();
        if (evaluationPeriod == null) {
            return RepeatStatus.FINISHED;
        }

        Long evaluationPeriodId = evaluationPeriod.getEvaluationPeriodId();
        Integer targetYear = evaluationPeriod.getEvaluationYear();
        String targetPeriod = String.valueOf(evaluationPeriodId);

        Map<Long, BigDecimal> productionSpeedScores = antiGamingSourceQueryMapper
            .findProductionSpeedScoresByEvaluationPeriodId(evaluationPeriodId)
            .stream()
            .filter(row -> row.getEmployeeId() != null && row.getProductionSpeedScore() != null)
            .collect(Collectors.toMap(
                AntiGamingProductionScoreRow::getEmployeeId,
                row -> row.getProductionSpeedScore().setScale(4, RoundingMode.HALF_UP),
                (left, right) -> right,
                LinkedHashMap::new
            ));

        Map<Long, BigDecimal> safetyScores = loadSafetyScores(evaluationPeriodId);

        List<AntiGamingFlagDecision> decisions = buildDecisions(productionSpeedScores, safetyScores);
        syncFlags(targetYear, targetPeriod, decisions);

        log.info(
            "Refreshed anti-gaming flags. evaluationPeriodId={}, targetYear={}, flaggedCount={}, candidateCount={}",
            evaluationPeriodId,
            targetYear,
            decisions.size(),
            productionSpeedScores.size()
        );
        return RepeatStatus.FINISHED;
    }

    /**
     * anti-gaming 산출 대상 월간 평가 기간을 조회한다.
     * @param 없음
     * @return 월간 평가 기간 projection
     */
    private EvaluationPeriodProjectionEntity resolveMonthlyEvaluationPeriod() {
        if (requestedEvaluationPeriodId != null) {
            EvaluationPeriodProjectionEntity entity = evaluationPeriodProjectionRepository.findById(requestedEvaluationPeriodId)
                .orElse(null);
            if (entity == null) {
                log.info("Skipping anti-gaming refresh. evaluationPeriodId={} was not found.", requestedEvaluationPeriodId);
                return null;
            }
            if (!"CONFIRMED".equalsIgnoreCase(entity.getStatus())) {
                log.info("Skipping anti-gaming refresh. evaluationPeriodId={} is not confirmed.", requestedEvaluationPeriodId);
                return null;
            }
            if (!isMonthlyPeriod(entity)) {
                log.info("Skipping anti-gaming refresh. evaluationPeriodId={} is not monthly.", requestedEvaluationPeriodId);
                return null;
            }
            return entity;
        }

        if (requestedPeriodType != null && !requestedPeriodType.isBlank()) {
            BatchPeriodType periodType = BatchPeriodType.valueOf(requestedPeriodType.trim().toUpperCase());
            if (periodType != BatchPeriodType.MONTH) {
                log.info("Skipping anti-gaming refresh because periodType is not MONTH. periodType={}", periodType);
                return null;
            }
        }

        return evaluationPeriodProjectionRepository.findAll().stream()
            .filter(entity -> "CONFIRMED".equalsIgnoreCase(entity.getStatus()))
            .filter(this::isMonthlyPeriod)
            .max(Comparator.comparing(EvaluationPeriodProjectionEntity::getEndDate))
            .orElse(null);
    }

    /**
     * 평가 기간이 월간 길이인지 확인한다.
     * @param entity 평가 기간 projection
     * @return 월간 여부
     */
    private boolean isMonthlyPeriod(EvaluationPeriodProjectionEntity entity) {
        long inclusiveDays = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        return inclusiveDays >= 28 && inclusiveDays <= 31;
    }

    /**
     * 정성 코멘트에서 안전 키워드 점수를 계산한다.
     * @param evaluationPeriodId 평가 기간 ID
     * @return 직원별 안전 키워드 점수 맵
     */
    private Map<Long, BigDecimal> loadSafetyScores(Long evaluationPeriodId) {
        Map<Long, List<MatchedKeywordDetail>> matchedKeywordDetailsByEmployee = new LinkedHashMap<>();
        for (MonthlyMatchedKeywordRow row : evaluationCommentQueryMapper.findMatchedKeywordsByEvaluationPeriodId(evaluationPeriodId)) {
            if (row.getEmployeeId() == null) {
                continue;
            }
            matchedKeywordDetailsByEmployee
                .computeIfAbsent(row.getEmployeeId(), ignored -> new ArrayList<>())
                .addAll(parseMatchedKeywordDetails(row.getMatchedKeywordDetails(), row.getMatchedKeywords()));
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<MatchedKeywordDetail>> entry : matchedKeywordDetailsByEmployee.entrySet()) {
            BigDecimal safetyScore = qualitativeSkillKeywordClassifier
                .classifyMatchedKeywords(entry.getValue())
                .getOrDefault(SAFETY_CATEGORY, ZERO)
                .setScale(4, RoundingMode.HALF_UP);
            result.put(entry.getKey(), safetyScore);
        }
        return result;
    }

    /**
     * anti-gaming 대상자 판단 결과를 계산한다.
     * @param productionSpeedScores 직원별 생산 속도 점수
     * @param safetyScores 직원별 안전 키워드 점수
     * @return anti-gaming 판단 결과 목록
     */
    private List<AntiGamingFlagDecision> buildDecisions(
        Map<Long, BigDecimal> productionSpeedScores,
        Map<Long, BigDecimal> safetyScores
    ) {
        if (productionSpeedScores.isEmpty() || safetyScores.isEmpty()) {
            return List.of();
        }

        Set<Long> candidateEmployeeIds = productionSpeedScores.keySet().stream()
            .filter(safetyScores::containsKey)
            .collect(Collectors.toSet());
        if (candidateEmployeeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> productionRanks = assignRanksDescending(productionSpeedScores, candidateEmployeeIds);
        Map<Long, Integer> safetyRanks = assignRanksDescending(safetyScores, candidateEmployeeIds);

        int candidateCount = candidateEmployeeIds.size();
        int cutoffRank = Math.max(1, (int) Math.ceil(candidateCount * OUTLIER_RATIO));
        int bottomRankThreshold = candidateCount - cutoffRank + 1;

        List<AntiGamingFlagDecision> decisions = new ArrayList<>();
        for (Long employeeId : candidateEmployeeIds) {
            Integer productionRank = productionRanks.get(employeeId);
            Integer safetyRank = safetyRanks.get(employeeId);
            if (productionRank == null || safetyRank == null) {
                continue;
            }
            if (productionRank <= cutoffRank && safetyRank >= bottomRankThreshold) {
                decisions.add(new AntiGamingFlagDecision(
                    employeeId,
                    productionRank,
                    safetyRank,
                    DEFAULT_PENALTY
                ));
            }
        }
        return decisions;
    }

    /**
     * 직원별 점수를 내림차순 등수로 변환한다.
     * @param scoreByEmployeeId 직원별 점수 맵
     * @param candidateEmployeeIds 등수 산정 대상 직원 집합
     * @return 직원별 등수 맵
     */
    private Map<Long, Integer> assignRanksDescending(
        Map<Long, BigDecimal> scoreByEmployeeId,
        Set<Long> candidateEmployeeIds
    ) {
        List<Map.Entry<Long, BigDecimal>> sortedEntries = candidateEmployeeIds.stream()
            .map(employeeId -> Map.entry(employeeId, scoreByEmployeeId.getOrDefault(employeeId, ZERO)))
            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed()
                .thenComparing(Map.Entry::getKey))
            .toList();

        Map<Long, Integer> ranks = new LinkedHashMap<>();
        BigDecimal previousScore = null;
        int rank = 0;
        for (int index = 0; index < sortedEntries.size(); index++) {
            Map.Entry<Long, BigDecimal> entry = sortedEntries.get(index);
            if (previousScore == null || previousScore.compareTo(entry.getValue()) != 0) {
                rank = index + 1;
                previousScore = entry.getValue();
            }
            ranks.put(entry.getKey(), rank);
        }
        return ranks;
    }

    /**
     * 산출 결과를 anti_gaming_flag 테이블에 동기화한다.
     * @param targetYear 대상 연도
     * @param targetPeriod 대상 기간 문자열
     * @param decisions anti-gaming 판단 결과
     */
    private void syncFlags(Integer targetYear, String targetPeriod, List<AntiGamingFlagDecision> decisions) {
        List<AntiGamingFlagEntity> existingFlags = antiGamingFlagRepository.findAllByTargetYearAndTargetPeriod(targetYear, targetPeriod);
        Map<Long, AntiGamingFlagEntity> existingByEmployeeId = existingFlags.stream()
            .collect(Collectors.toMap(AntiGamingFlagEntity::getEmployeeId, entity -> entity, (left, right) -> right, LinkedHashMap::new));
        Map<Long, AntiGamingFlagDecision> decisionByEmployeeId = decisions.stream()
            .collect(Collectors.toMap(AntiGamingFlagDecision::employeeId, decision -> decision, (left, right) -> right, LinkedHashMap::new));

        List<AntiGamingFlagEntity> dirtyEntities = new ArrayList<>();
        for (AntiGamingFlagDecision decision : decisions) {
            AntiGamingFlagEntity existing = existingByEmployeeId.get(decision.employeeId());
            if (existing == null) {
                dirtyEntities.add(AntiGamingFlagEntity.create(
                    idGenerator.generate(),
                    decision.employeeId(),
                    decision.productionSpeedRank(),
                    decision.safetyKeywordRank(),
                    decision.penaltyCoefficient(),
                    targetYear,
                    targetPeriod,
                    java.time.LocalDateTime.now()
                ));
                continue;
            }
            existing.refresh(
                decision.productionSpeedRank(),
                decision.safetyKeywordRank(),
                decision.penaltyCoefficient(),
                java.time.LocalDateTime.now()
            );
            dirtyEntities.add(existing);
        }

        for (AntiGamingFlagEntity existing : existingFlags) {
            if (decisionByEmployeeId.containsKey(existing.getEmployeeId())) {
                continue;
            }
            if (!Boolean.TRUE.equals(existing.getIsActive())) {
                continue;
            }
            existing.deactivate(java.time.LocalDateTime.now());
            dirtyEntities.add(existing);
        }

        if (!dirtyEntities.isEmpty()) {
            antiGamingFlagRepository.saveAll(dirtyEntities);
        }
    }

    /**
     * 매칭 키워드 JSON 을 상세 객체 목록으로 변환한다.
     * @param detailsJson 상세 키워드 JSON
     * @param keywordsJson 키워드 목록 JSON
     * @return 매칭 키워드 상세 목록
     */
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
                .filter(Objects::nonNull)
                .map(value -> new MatchedKeywordDetail(null, value, null, null))
                .toList();
        } catch (Exception exception) {
            log.warn(
                "Failed to parse anti-gaming matched keyword payload. detailsPayload={}, keywordsPayload={}",
                detailsJson,
                keywordsJson,
                exception
            );
            return List.of();
        }
    }

    private record AntiGamingFlagDecision(
        Long employeeId,
        Integer productionSpeedRank,
        Integer safetyKeywordRank,
        BigDecimal penaltyCoefficient
    ) {
    }
}
