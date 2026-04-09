package com.ohgiraffers.team3backendbatch.batch.job.promotion.reader;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

/**
 * 승진 후보 판정을 위한 현재 상태를 읽는 Reader 스켈레톤이다.
 *
 * 예상 기능:
 * - score current 값 조회
 * - employee current tier 조회
 * - tier_config 조회
 * - 기존 promotion_history의 진행 중 상태가 있는 직원 제외
 * - period 기준 적합한 판정 대상만 읽기
 */
@Component
public class PromotionCandidateReader implements ItemReader<PromotionCandidateSnapshot> {

    private static final Logger log = LoggerFactory.getLogger(PromotionCandidateReader.class);

    private boolean logged;

    @Override
    public PromotionCandidateSnapshot read() {
        if (!logged) {
            logged = true;
            log.info("PromotionCandidateReader chunk skeleton invoked. TODO replace with official-score reader.");
        }
        return null;
    }
}