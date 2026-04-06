package com.ohgiraffers.team3backendbatch.batch.job.score.writer;

import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 기간별 performance_point 상세 이력을 적재하는 Writer 스켈레톤이다.
 *
 * 예상 기능:
 * - point_type별 row 생성
 * - quantitative / qualitative / kms / integrated 출처 기록
 * - point_source_id 및 point_source_type 저장
 * - 재실행 시 중복 insert 방지 또는 replace 정책 적용
 *
 * 이 테이블은 추후 KPI 추세, 점수 설명, 이의제기 대응 근거로 사용될 가능성이 크다.
 */
@Component
public class PerformancePointWriter implements ItemWriter<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(PerformancePointWriter.class);

    @Override
    public void write(Chunk<? extends IntegratedScoreAggregate> chunk) {
        log.info("PerformancePointWriter chunk skeleton invoked. itemCount={}", chunk.size());
        // TODO performance_point history insert
    }
}