package com.ohgiraffers.team3backendbatch.batch.job.qualitative.reader;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.model.QualitativeEvaluationAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

/**
 * 정성 평가 원문을 읽는 Reader 스켈레톤이다.
 *
 * 예상 기능:
 * - HR 소유 qualitative_evaluation 에서 분석 대상 코멘트 조회
 * - 미분석 또는 재분석이 필요한 평가만 선별
 * - evaluation_period, employee, evaluator 정보를 함께 로드
 * - 필요 시 input_method(STT/TEXT) 구분 정보를 함께 전달
 */
@Component
public class QualitativeEvaluationReader implements ItemReader<QualitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationReader.class);

    private boolean logged;

    @Override
    public QualitativeEvaluationAggregate read() {
        if (!logged) {
            logged = true;
            log.info("QualitativeEvaluationReader skeleton invoked. TODO replace with HR comment reader.");
        }
        return null;
    }
}