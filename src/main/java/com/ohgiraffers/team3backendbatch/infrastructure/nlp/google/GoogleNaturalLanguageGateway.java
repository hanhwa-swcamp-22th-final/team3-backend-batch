package com.ohgiraffers.team3backendbatch.infrastructure.nlp.google;

import com.google.cloud.language.v1.AnnotateTextRequest;
import com.google.cloud.language.v1.AnnotateTextResponse;
import com.google.cloud.language.v1.DependencyEdge;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.PartOfSpeech;
import com.google.cloud.language.v1.Token;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.NlpAnalysisGateway;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.dto.NlpAnalysisResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Google Natural Language API gateway based on annotateText.
 *
 * TODO 운영 보강 포인트:
 * - Google API 예외를 배치 도메인 예외로 감싸고 retry 대상/비대상 예외를 구분한다.
 * - 필요 시 language/hint 설정이나 한국어 전용 전처리를 추가한다.
 * - 청크별 상세 응답 전체를 저장할 필요가 있으면 별도 DTO 또는 raw payload 저장 전략을 추가한다.
 */
@Component
public class GoogleNaturalLanguageGateway implements NlpAnalysisGateway {

    private final LanguageServiceClient languageServiceClient;

    public GoogleNaturalLanguageGateway(LanguageServiceClient languageServiceClient) {
        this.languageServiceClient = languageServiceClient;
    }

    @Override
    public NlpAnalysisResponse annotateText(String text) {
        if (!StringUtils.hasText(text)) {
            return new NlpAnalysisResponse(BigDecimal.ZERO, false, List.of());
        }

        AnnotateTextResponse response = languageServiceClient.annotateText(buildRequest(text));

        BigDecimal sentimentScore = response.hasDocumentSentiment()
            ? BigDecimal.valueOf(response.getDocumentSentiment().getScore())
            : BigDecimal.ZERO;

        boolean negationDetected = response.getTokensList().stream()
            .map(Token::getDependencyEdge)
            .map(DependencyEdge::getLabel)
            .anyMatch(label -> label == DependencyEdge.Label.NEG);

        List<String> keywordLemmas = response.getTokensList().stream()
            .filter(token -> isKeywordToken(token.getPartOfSpeech().getTag()))
            .map(token -> normalizeLemma(token.getLemma(), token.getText().getContent()))
            .filter(StringUtils::hasText)
            .distinct()
            .limit(20)
            .toList();

        return new NlpAnalysisResponse(sentimentScore, negationDetected, keywordLemmas);
    }

    private AnnotateTextRequest buildRequest(String text) {
        Document document = Document.newBuilder()
            .setContent(text)
            .setType(Document.Type.PLAIN_TEXT)
            .build();

        AnnotateTextRequest.Features features = AnnotateTextRequest.Features.newBuilder()
            .setExtractSyntax(true)
            .setExtractDocumentSentiment(true)
            .build();

        return AnnotateTextRequest.newBuilder()
            .setDocument(document)
            .setFeatures(features)
            .setEncodingType(EncodingType.UTF8)
            .build();
    }

    private boolean isKeywordToken(PartOfSpeech.Tag tag) {
        return tag == PartOfSpeech.Tag.NOUN || tag == PartOfSpeech.Tag.VERB;
    }

    private String normalizeLemma(String lemma, String fallbackText) {
        String value = StringUtils.hasText(lemma) ? lemma.trim() : fallbackText;
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}