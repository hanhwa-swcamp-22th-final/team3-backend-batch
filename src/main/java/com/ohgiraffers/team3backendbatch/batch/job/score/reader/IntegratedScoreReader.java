package com.ohgiraffers.team3backendbatch.batch.job.score.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.QuantitativeEvaluationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class IntegratedScoreReader implements ItemReader<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(IntegratedScoreReader.class);

    private final EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;
    private final EmployeeProjectionRepository employeeProjectionRepository;
    private final QuantitativeEvaluationRepository quantitativeEvaluationRepository;
    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

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
        EvaluationPeriodProjectionEntity evaluationPeriod = evaluationPeriodProjectionRepository
            .findLatestConfirmedMonthlyPeriod()
            .orElse(null);

        if (evaluationPeriod == null) {
            log.info("Skipping score aggregation. No confirmed monthly evaluation period was found.");
            return List.of();
        }

        Long evaluationPeriodId = evaluationPeriod.getEvaluationPeriodId();
        LocalDate pointEarnedDate = evaluationPeriod.getEndDate();
        LocalDateTime occurredAt = LocalDateTime.now();

        Map<Long, BigDecimal> quantitativeScores = quantitativeEvaluationRepository
            .findAverageSettledScoresByEvaluationPeriodId(evaluationPeriodId)
            .stream()
            .collect(Collectors.toMap(
                QuantitativeEvaluationRepository.MonthlyQuantitativeScoreView::getEmployeeId,
                QuantitativeEvaluationRepository.MonthlyQuantitativeScoreView::getAverageTScore
            ));

        Map<Long, BigDecimal> qualitativeScores = qualitativeScoreProjectionRepository
            .findLatestNormalizedScoresByEvaluationPeriodId(evaluationPeriodId)
            .stream()
            .collect(Collectors.toMap(
                QualitativeScoreProjectionRepository.MonthlyQualitativeScoreView::getEmployeeId,
                QualitativeScoreProjectionRepository.MonthlyQualitativeScoreView::getNormalizedScore
            ));

        List<IntegratedScoreAggregate> items = employeeProjectionRepository.findAll().stream()
            .filter(this::isEligibleEmployee)
            .map(employee -> buildAggregate(employee, evaluationPeriodId, pointEarnedDate, occurredAt, quantitativeScores, qualitativeScores))
            .filter(Objects::nonNull)
            .toList();

        log.info(
            "Prepared integrated score targets. evaluationPeriodId={}, employeeCount={}, quantitativeCount={}, qualitativeCount={}",
            evaluationPeriodId,
            items.size(),
            quantitativeScores.size(),
            qualitativeScores.size()
        );
        return items;
    }

    private IntegratedScoreAggregate buildAggregate(
        EmployeeProjectionEntity employee,
        Long evaluationPeriodId,
        LocalDate pointEarnedDate,
        LocalDateTime occurredAt,
        Map<Long, BigDecimal> quantitativeScores,
        Map<Long, BigDecimal> qualitativeScores
    ) {
        BigDecimal quantitativeTScore = quantitativeScores.get(employee.getEmployeeId());
        BigDecimal qualitativeScore = qualitativeScores.get(employee.getEmployeeId());

        if (quantitativeTScore == null && qualitativeScore == null) {
            return null;
        }

        return IntegratedScoreAggregate.builder()
            .employeeId(employee.getEmployeeId())
            .evaluationPeriodId(evaluationPeriodId)
            .periodType(BatchPeriodType.MONTH)
            .pointEarnedDate(pointEarnedDate)
            .occurredAt(occurredAt)
            .quantitativeTScore(quantitativeTScore)
            .qualitativeScore(qualitativeScore)
            .performancePointEvents(List.of())
            .build();
    }

    private boolean isEligibleEmployee(EmployeeProjectionEntity employee) {
        if (employee.getEmployeeId() == null) {
            return false;
        }
        return employee.getEmployeeStatus() == null || !"INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
    }
}
