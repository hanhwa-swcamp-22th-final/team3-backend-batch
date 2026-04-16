package com.ohgiraffers.team3backendbatch.batch.job.skillscore.writer;

import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 현재 종합 점수 테이블(score)을 갱신하는 Writer 스켈레톤이다.
 *
 * 예상 기능:
 * - employee 기준 현재 capability_index 업데이트
 * - total_points 업데이트
 * - tier 업데이트
 * - evaluation_year / evaluation_period 기록
 * - 수동 재실행에서도 같은 기간 값을 재반영할 수 있도록 upsert 정책 적용
 */
@Component
public class ScoreWriter implements ItemWriter<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(ScoreWriter.class);

    /**
     * 종합 점수 projection 저장 처리를 수행한다.
     * @param chunk 종합 점수 저장 대상 청크
     * @return 반환값 없음
     */
    @Override
    public void write(Chunk<? extends IntegratedScoreAggregate> chunk) {
        log.info("ScoreWriter chunk skeleton invoked. itemCount={}", chunk.size());
        // TODO score current 값 upsert
    }
}
