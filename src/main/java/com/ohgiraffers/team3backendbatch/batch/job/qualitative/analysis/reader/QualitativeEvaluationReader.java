package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.reader;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeEvaluationQueryMapper;
@Component
@StepScope
public class QualitativeEvaluationReader implements ItemReader<QualitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationReader.class);
    private final QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper;
    private final Long evaluationPeriodId;
    private final Long employeeId;
    private final Long qualitativeEvaluationId;
    private final boolean force;
    private final String analysisVersion;
    private Iterator<QualitativeEvaluationAggregate> iterator;

    /**
     * 실행 시 전달된 job parameter를 기준으로 어떤 평가 건을 읽을지 결정한다.
     */
    public QualitativeEvaluationReader(
        QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['employeeId']}") Long employeeId,
        @Value("#{jobParameters['qualitativeEvaluationId']}") Long qualitativeEvaluationId,
        @Value("#{jobParameters['force']}") String force,
        @Value("#{jobParameters['analysisVersion']}") String analysisVersion) {
        this.qualitativeEvaluationQueryMapper = qualitativeEvaluationQueryMapper;
        this.evaluationPeriodId = evaluationPeriodId;
        this.employeeId = employeeId;
        this.qualitativeEvaluationId = qualitativeEvaluationId;
        this.force = Boolean.parseBoolean(force);
        this.analysisVersion = analysisVersion != null ? analysisVersion : "squal-v1";
        }

    /**
     * 분석 대상 목록을 최초 1회 조회한 뒤, 호출될 때마다 한 건씩 반환한다.
     * 더 이상 대상이 없으면 null을 반환해 step 종료를 알린다.
     */
    @Override
    public QualitativeEvaluationAggregate read() {
        if (iterator == null) {
            List<QualitativeEvaluationAggregate> evaluations =
                qualitativeEvaluationQueryMapper.findQualitativeEvaluationsForAnalysis(
                    evaluationPeriodId,
                    employeeId,
                    qualitativeEvaluationId,
                    force,
                    analysisVersion
                );
            iterator = evaluations.iterator();
            log.info(
                "Loaded qualitative evaluations. evaluationPeriodId={}, employeeId={}, qualitativeEvaluationId={}, force={}, count={}",
                evaluationPeriodId,
                employeeId,
                qualitativeEvaluationId,
                force,
                evaluations.size()
            );
        }
        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }
}