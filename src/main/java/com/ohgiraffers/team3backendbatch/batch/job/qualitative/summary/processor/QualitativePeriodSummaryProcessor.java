package com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryTarget;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativePeriodSummaryProcessor
    implements ItemProcessor<QualitativePeriodSummaryTarget, QualitativePeriodSummaryResult> {

    private final QualitativeScoreCalculator qualitativeScoreCalculator;

    /**
     * 정성 요약 대상 데이터를 요약 결과로 변환한다.
     * @param item 정성 요약 대상 데이터
     * @return 정성 기간 요약 결과
     */
    @Override
    public QualitativePeriodSummaryResult process(QualitativePeriodSummaryTarget item) {
        String grade = qualitativeScoreCalculator.classifyTier(item.getAverageNormalizedScore());

        return QualitativePeriodSummaryResult.builder()
            .evaluationPeriodId(item.getEvaluationPeriodId())
            .evaluateeId(item.getEvaluateeId())
            .periodType(item.getPeriodType())
            .sourceMonthCount(item.getSourceMonthCount())
            .averageRawScore(item.getAverageRawScore())
            .averageNormalizedScore(item.getAverageNormalizedScore())
            .grade(grade)
            .build();
    }
}
