package com.ohgiraffers.team3backendbatch.batch.job.score.reader;

import com.ohgiraffers.team3backendbatch.batch.job.score.model.IntegratedScoreAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

/**
 * 통합 점수 계산에 필요한 데이터를 읽는 Reader 스켈레톤이다.
 *
 * 예상 기능:
 * - quantitative_evaluation 최종 레코드 읽기
 * - qualitative_evaluation 최종확정 레코드 읽기
 * - KMS 승인 문서 건수 또는 카테고리별 기여값 읽기
 * - 기존 score/skill 현재값 읽기
 * - employee 기준으로 한 번에 계산 가능한 형태로 조합
 */
@Component
public class IntegratedScoreReader implements ItemReader<IntegratedScoreAggregate> {

    private static final Logger log = LoggerFactory.getLogger(IntegratedScoreReader.class);

    private boolean logged;

    @Override
    public IntegratedScoreAggregate read() {
        if (!logged) {
            logged = true;
            log.info("IntegratedScoreReader chunk skeleton invoked. TODO replace with settlement-aware DB reader.");
        }
        return null;
    }
}