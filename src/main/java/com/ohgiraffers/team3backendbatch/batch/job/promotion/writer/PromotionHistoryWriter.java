package com.ohgiraffers.team3backendbatch.batch.job.promotion.writer;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 승진 후보 결과를 promotion_history에 반영하는 Writer 스켈레톤이다.
 *
 * 예상 기능:
 * - employee + period 기준 신규 row 생성
 * - 기존 값이 있으면 상태 갱신 또는 중복 방지
 * - current_tier_config_id / target_tier_config_id 기록
 * - tier_accumulated_point 저장
 * - 추후 HR 승인 흐름과 충돌하지 않도록 상태 전이 규칙 고려
 */
@Component
public class PromotionHistoryWriter implements ItemWriter<PromotionCandidateSnapshot> {

    private static final Logger log = LoggerFactory.getLogger(PromotionHistoryWriter.class);

    @Override
    public void write(Chunk<? extends PromotionCandidateSnapshot> chunk) {
        log.info("PromotionHistoryWriter chunk skeleton invoked. itemCount={}", chunk.size());
        // TODO promotion_history insert / update
    }
}