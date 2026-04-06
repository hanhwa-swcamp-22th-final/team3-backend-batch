package com.ohgiraffers.team3backendbatch.infrastructure.nlp;

/**
 * 외부 NLP 분석기 연동 추상화 스켈레톤이다.
 *
 * 현재는 Google Natural Language API 를 우선 대상으로 하지만,
 * 구현체를 교체해도 배치 정성 계산 로직이 흔들리지 않도록 gateway 로 분리한다.
 */
public interface NlpAnalysisGateway {
    // TODO annotateText(...)
}