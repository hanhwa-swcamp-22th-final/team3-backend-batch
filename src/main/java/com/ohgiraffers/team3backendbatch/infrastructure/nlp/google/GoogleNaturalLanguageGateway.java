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
import org.springframework.util.StringUtils;

/**
 * Google Natural Language API gateway based on annotateText.
 */
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