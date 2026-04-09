package com.ohgiraffers.team3backendbatch.batch.job.score.writer;

import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 카테고리별 skill current 값을 반영하는 Writer 스켈레톤이다.
 *
 * 예상 기능:
 * - employee + skill_category 기준 score 업데이트
 * - evaluated_at 갱신
 * - 필요 시 skill tier 산출
 * - Admin 수동 보정값이 있다면 최종 반영 직전에 병합하는 훅 제공
 */
@Component
public class SkillWriter implements ItemWriter<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(SkillWriter.class);

    @Override
    public void write(Chunk<? extends IntegratedScoreAggregate> chunk) {
        log.info("SkillWriter chunk skeleton invoked. itemCount={}", chunk.size());
        // TODO skill current 값 update / upsert
    }
}