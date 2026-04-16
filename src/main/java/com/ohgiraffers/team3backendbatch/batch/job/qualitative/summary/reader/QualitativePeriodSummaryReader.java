package com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryTarget;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativePeriodSummaryCandidateRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativePeriodSummaryQueryMapper;
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

    private final QualitativePeriodSummaryQueryMapper qualitativePeriodSummaryQueryMapper;
    private final Long evaluationPeriodId;
    private final Long employeeId;
    private final BatchPeriodType periodType;
    private Iterator<QualitativePeriodSummaryTarget> iterator;

    public QualitativePeriodSummaryReader(
        QualitativePeriodSummaryQueryMapper qualitativePeriodSummaryQueryMapper,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['employeeId']}") Long employeeId,
        @Value("#{jobParameters['periodType']}") String periodType
    ) {
        this.qualitativePeriodSummaryQueryMapper = qualitativePeriodSummaryQueryMapper;
        this.evaluationPeriodId = evaluationPeriodId;
        this.employeeId = employeeId;
        this.periodType = parsePeriodType(periodType);
    }

    /**
     * 정성 요약 대상 데이터를 한 건씩 반환한다.
     * @param 없음
     * @return 정성 요약 대상 데이터
     */
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

            List<QualitativePeriodSummaryTarget> items = qualitativePeriodSummaryQueryMapper
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

    /**
     * 문자열 periodType 값을 배치 enum 으로 변환한다.
     * @param value job parameter 로 전달된 periodType 문자열
     * @return 변환된 배치 기간 유형
     */
    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.MONTH;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    /**
     * 상위 기간 요약 대상인지 확인한다.
     * @param value 배치 기간 유형
     * @return 상위 기간이면 true, 아니면 false
     */
    private boolean isUpperPeriod(BatchPeriodType value) {
        return value == BatchPeriodType.QUARTER
            || value == BatchPeriodType.HALF_YEAR
            || value == BatchPeriodType.YEAR;
    }
}
