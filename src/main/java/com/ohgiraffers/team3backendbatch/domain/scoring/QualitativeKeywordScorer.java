package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Scores domain keywords against a comment chunk.
 */
@Component
public class QualitativeKeywordScorer {

    private static final BigDecimal KEYWORD_SUM_CAP = BigDecimal.valueOf(0.60);
    private static final BigDecimal CONTEXT_BASE = BigDecimal.ONE;
    private static final BigDecimal CONTEXT_DETAIL = BigDecimal.valueOf(1.05);
    private static final BigDecimal CONTEXT_DETAIL_WITH_NUMBER = BigDecimal.valueOf(1.10);

    private static final Map<String, BigDecimal> KEYWORD_WEIGHTS = new LinkedHashMap<>();

    static {
        KEYWORD_WEIGHTS.put("설비 정비", BigDecimal.valueOf(0.30));
        KEYWORD_WEIGHTS.put("불량 저감", BigDecimal.valueOf(0.25));
        KEYWORD_WEIGHTS.put("수율", BigDecimal.valueOf(0.20));
        KEYWORD_WEIGHTS.put("리드타임", BigDecimal.valueOf(0.15));
        KEYWORD_WEIGHTS.put("제안", BigDecimal.valueOf(0.20));
        KEYWORD_WEIGHTS.put("전수", BigDecimal.valueOf(0.20));
        KEYWORD_WEIGHTS.put("분석", BigDecimal.valueOf(0.15));
        KEYWORD_WEIGHTS.put("반복 불량", BigDecimal.valueOf(-0.20));
        KEYWORD_WEIGHTS.put("실수", BigDecimal.valueOf(-0.15));
        KEYWORD_WEIGHTS.put("미흡", BigDecimal.valueOf(-0.10));

        KEYWORD_WEIGHTS.put("equipment maintenance", BigDecimal.valueOf(0.30));
        KEYWORD_WEIGHTS.put("defect reduction", BigDecimal.valueOf(0.25));
        KEYWORD_WEIGHTS.put("yield", BigDecimal.valueOf(0.20));
        KEYWORD_WEIGHTS.put("lead time", BigDecimal.valueOf(0.15));
        KEYWORD_WEIGHTS.put("proposal", BigDecimal.valueOf(0.20));
        KEYWORD_WEIGHTS.put("full inspection", BigDecimal.valueOf(0.20));
        KEYWORD_WEIGHTS.put("analysis", BigDecimal.valueOf(0.15));
        KEYWORD_WEIGHTS.put("repeat defect", BigDecimal.valueOf(-0.20));
        KEYWORD_WEIGHTS.put("mistake", BigDecimal.valueOf(-0.15));
        KEYWORD_WEIGHTS.put("insufficient", BigDecimal.valueOf(-0.10));
    }

    public KeywordScoreResult scoreKeywords(String text) {
        return scoreKeywords(text, List.of());
    }

    public KeywordScoreResult scoreKeywords(String text, List<String> extractedLemmas) {
        String normalizedText = normalize(text);
        List<String> normalizedLemmas = extractedLemmas == null
            ? List.of()
            : extractedLemmas.stream().map(this::normalize).filter(value -> !value.isBlank()).toList();

        BigDecimal total = BigDecimal.ZERO;
        Set<String> matchedKeywords = new LinkedHashSet<>();

        for (Map.Entry<String, BigDecimal> entry : KEYWORD_WEIGHTS.entrySet()) {
            String keyword = normalize(entry.getKey());
            if (matchesKeyword(normalizedText, normalizedLemmas, keyword)) {
                total = total.add(entry.getValue());
                matchedKeywords.add(entry.getKey());
            }
        }

        return new KeywordScoreResult(
            clampKeywordWeightSum(total),
            matchedKeywords.size(),
            new ArrayList<>(matchedKeywords)
        );
    }

    public BigDecimal determineContextWeight(String text, int matchedKeywordCount) {
        boolean hasNumber = text != null && text.matches(".*\\d+.*");
        if (matchedKeywordCount >= 3 && hasNumber) {
            return CONTEXT_DETAIL_WITH_NUMBER;
        }
        if (matchedKeywordCount >= 2) {
            return CONTEXT_DETAIL;
        }
        return CONTEXT_BASE;
    }

    private BigDecimal clampKeywordWeightSum(BigDecimal total) {
        if (total.compareTo(KEYWORD_SUM_CAP.negate()) < 0) {
            return KEYWORD_SUM_CAP.negate().setScale(4, RoundingMode.HALF_UP);
        }
        if (total.compareTo(KEYWORD_SUM_CAP) > 0) {
            return KEYWORD_SUM_CAP.setScale(4, RoundingMode.HALF_UP);
        }
        return total.setScale(4, RoundingMode.HALF_UP);
    }

    private boolean matchesKeyword(String normalizedText, List<String> normalizedLemmas, String keyword) {
        if (normalizedText.contains(keyword)) {
            return true;
        }
        return normalizedLemmas.stream().anyMatch(lemma -> lemma.contains(keyword) || keyword.contains(lemma));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}