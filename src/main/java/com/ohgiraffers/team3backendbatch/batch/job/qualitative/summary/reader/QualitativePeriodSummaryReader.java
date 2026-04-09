package com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryTarget;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativePeriodSummaryProjectionRepository;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class QualitativePeriodSummaryReader implements ItemReader<QualitativePeriodSummaryTarget> {

    private final QualitativePeriodSummaryProjectionRepository qualitativePeriodSummaryProjectionRepository;
    private final Long evaluationPeriodId;
    private final Long employeeId;
    private final BatchPeriodType periodType;
    private Iterator<QualitativePeriodSummaryTarget> iterator;

    public QualitativePeriodSummaryReader(
        QualitativePeriodSummaryProjectionRepository qualitativePeriodSummaryProjectionRepository,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['employeeId']}") Long employeeId,
        @Value("#{jobParameters['periodType']}") String periodType
    ) {
        this.qualitativePeriodSummaryProjectionRepository = qualitativePeriodSummaryProjectionRepository;
        this.evaluationPeriodId = evaluationPeriodId;
        this.employeeId = employeeId;
        this.periodType = parsePeriodType(periodType);
    }

    @Override
    public QualitativePeriodSummaryTarget read() {
        if (iterator == null) {
            if (evaluationPeriodId == null) {
                log.warn("No evaluationPeriodId job parameter provided for qualitative period summary job.");
                return null;
            }

            if (!isUpperPeriod(periodType)) {
                log.info(
                    "Skipping qualitative period summary because periodType is not an upper period. evaluationPeriodId={}, periodType={}",
                    evaluationPeriodId,
                    periodType
                );
                iterator = List.<QualitativePeriodSummaryTarget>of().iterator();
                return null;
            }

            List<QualitativePeriodSummaryTarget> items = qualitativePeriodSummaryProjectionRepository
                .findSummaryCandidates(evaluationPeriodId, employeeId)
                .stream()
                .map(candidate -> QualitativePeriodSummaryTarget.builder()
                    .evaluationPeriodId(candidate.getEvaluationPeriodId())
                    .evaluateeId(candidate.getEvaluateeId())
                    .periodType(periodType)
                    .sourceMonthCount(candidate.getSourceMonthCount() == null ? 0 : candidate.getSourceMonthCount().intValue())
                    .averageRawScore(candidate.getAverageRawScore())
                    .averageNormalizedScore(candidate.getAverageNormalizedScore())
                    .build())
                .toList();

            log.info(
                "Loaded qualitative period summary targets. evaluationPeriodId={}, employeeId={}, periodType={}, count={}",
                evaluationPeriodId,
                employeeId,
                periodType,
                items.size()
            );
            iterator = items.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }

    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.MONTH;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    private boolean isUpperPeriod(BatchPeriodType value) {
        return value == BatchPeriodType.QUARTER
            || value == BatchPeriodType.HALF_YEAR
            || value == BatchPeriodType.YEAR;
    }
}
