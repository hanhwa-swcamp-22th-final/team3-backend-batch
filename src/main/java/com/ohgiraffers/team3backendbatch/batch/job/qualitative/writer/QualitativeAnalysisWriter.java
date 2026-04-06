package com.ohgiraffers.team3backendbatch.batch.job.qualitative.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.model.QualitativeAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 정성 분석 결과를 저장하는 Writer 스켈레톤이다.
 *
 * 예상 기능:
 * - qualitative_analysis_result 전용 테이블 upsert
 * - 필요 시 qualitative_evaluation 의 최종 정성 점수 컬럼 동기화
 * - 알고리즘 버전, 분석 시각, 상태 저장
 * - ScoreAggregationJob 에서 읽을 수 있는 공식 정성 점수 결과로 정리
 */
@Component
public class QualitativeAnalysisWriter implements ItemWriter<QualitativeAnalysisResult> {

    private static final Logger log = LoggerFactory.getLogger(QualitativeAnalysisWriter.class);

    @Override
    public void write(Chunk<? extends QualitativeAnalysisResult> chunk) {
        log.info("QualitativeAnalysisWriter skeleton invoked. itemCount={}", chunk.size());
        // TODO 정성 분석 결과 upsert 구현
    }
}