package com.ohgiraffers.team3backendbatch.domain.scoring;

import org.springframework.stereotype.Component;

/**
 * lemma 기반 도메인 키워드/행동 동사 점수를 계산하는 스켈레톤이다.
 *
 * 책임:
 * - NOUN / VERB 토큰 필터링
 * - Admin 관리형 키워드 사전과 매칭
 * - 키워드 가중치 합산
 * - 카테고리별 skill share 계산에 필요한 메타데이터 생성
 */
@Component
public class QualitativeKeywordScorer {

    // TODO sumKeywordWeight(...)
    // TODO extractSkillCategoryShare(...)
}