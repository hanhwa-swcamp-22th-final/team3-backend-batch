package com.ohgiraffers.team3backendbatch.batch.job.qualitative.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.model.QualitativeEvaluationAggregate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 정성 평가 원문을 정성 점수 결과로 변환하는 Processor 스켈레톤이다.
 *
 * 예상 기능:
 * - 청크 분리
 * - Google NL API annotateText 호출
 * - NOUN/VERB lemma 기반 키워드 매칭
 * - NEG 보정
 * - 청크별 score 산정 및 가중 평균
 * - Squal_raw -> S_qual 정규화
 */
@Component
public class QualitativeEvaluationProcessor
    implements ItemProcessor<QualitativeEvaluationAggregate, QualitativeAnalysisResult> {

    @Override
    public QualitativeAnalysisResult process(QualitativeEvaluationAggregate item) {
        // TODO QualitativeScoreCalculator 와 NLP gateway 를 이용해 결과 모델 생성
        return new QualitativeAnalysisResult();
    }
}