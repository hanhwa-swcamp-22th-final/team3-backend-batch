package com.ohgiraffers.team3backendbatch.domain.scoring;

import org.springframework.stereotype.Component;

/**
 * 평가 코멘트 원문을 역접/정보 전환 기준으로 청크 분리하는 스켈레톤이다.
 *
 * 책임:
 * - ~지만, 그러나, 반면 같은 역접 표현 감지
 * - contrastive 청크 여부 판단
 * - Google NL API 호출 전에 분석 단위를 정리
 */
@Component
public class QualitativeChunkSplitter {

    // TODO splitIntoChunks(...)
    // TODO detectContrastiveChunk(...)
}