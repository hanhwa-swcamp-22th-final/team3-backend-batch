package com.ohgiraffers.team3backendbatch.infrastructure.nlp.google;

import com.ohgiraffers.team3backendbatch.infrastructure.nlp.NlpAnalysisGateway;
import org.springframework.stereotype.Component;

/**
 * Google Natural Language API 연동 구현체 스켈레톤이다.
 *
 * 예상 기능:
 * - annotateText 호출
 * - extractSyntax, extractDocumentSentiment 옵션 사용
 * - UTF8 encodingType 설정
 * - 외부 응답을 내부 NlpAnalysisResponse 로 변환
 */
@Component
public class GoogleNaturalLanguageGateway implements NlpAnalysisGateway {
    // TODO Google NL API 호출 구현
}