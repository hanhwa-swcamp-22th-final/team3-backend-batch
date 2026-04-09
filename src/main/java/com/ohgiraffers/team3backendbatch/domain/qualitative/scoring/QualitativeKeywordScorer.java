package com.ohgiraffers.team3backendbatch.domain.qualitative.scoring;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.KeywordScoreResult;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
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

@Component
public class QualitativeKeywordScorer {

    private static final BigDecimal KEYWORD_SUM_CAP = BigDecimal.valueOf(0.60);
    private static final BigDecimal CONTEXT_BASE = BigDecimal.ONE;
    private static final BigDecimal CONTEXT_DETAIL = BigDecimal.valueOf(1.05);
    private static final BigDecimal CONTEXT_DETAIL_WITH_NUMBER = BigDecimal.valueOf(1.10);

    private static final List<QualitativeKeywordRule> DEFAULT_POSITIVE_RULES = List.of(
        new QualitativeKeywordRule("equipment maintenance", BigDecimal.valueOf(0.30)),
        new QualitativeKeywordRule("defect reduction", BigDecimal.valueOf(0.25)),
        new QualitativeKeywordRule("yield", BigDecimal.valueOf(0.20)),
        new QualitativeKeywordRule("lead time", BigDecimal.valueOf(0.15)),
        new QualitativeKeywordRule("proposal", BigDecimal.valueOf(0.20)),
        new QualitativeKeywordRule("full inspection", BigDecimal.valueOf(0.20)),
        new QualitativeKeywordRule("analysis", BigDecimal.valueOf(0.15))
    );

    private static final Map<String, BigDecimal> NEGATIVE_KEYWORD_WEIGHTS = new LinkedHashMap<>();

    static {
        NEGATIVE_KEYWORD_WEIGHTS.put("repeat defect", BigDecimal.valueOf(-0.20));
        NEGATIVE_KEYWORD_WEIGHTS.put("mistake", BigDecimal.valueOf(-0.15));
        NEGATIVE_KEYWORD_WEIGHTS.put("insufficient", BigDecimal.valueOf(-0.10));
    }

    public KeywordScoreResult scoreKeywords(String text) {
        return scoreKeywords(text, List.of(), List.of());
    }

    public KeywordScoreResult scoreKeywords(String text, List<String> extractedLemmas) {
        return scoreKeywords(text, extractedLemmas, List.of());
    }

    public KeywordScoreResult scoreKeywords(String text, List<String> extractedLemmas, List<QualitativeKeywordRule> keywordRules) {
        String normalizedText = normalize(text);
        List<String> normalizedLemmas = extractedLemmas == null
            ? List.of()
            : extractedLemmas.stream().map(this::normalize).filter(value -> !value.isBlank()).toList();

        BigDecimal total = BigDecimal.ZERO;
        Set<String> matchedKeywords = new LinkedHashSet<>();

        for (QualitativeKeywordRule rule : resolvePositiveRules(keywordRules)) {
            String keyword = normalize(rule.getKeyword());
            if (matchesKeyword(normalizedText, normalizedLemmas, keyword)) {
                total = total.add(rule.getScoreWeight());
                matchedKeywords.add(rule.getKeyword());
            }
        }

        for (Map.Entry<String, BigDecimal> entry : NEGATIVE_KEYWORD_WEIGHTS.entrySet()) {
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

    private List<QualitativeKeywordRule> resolvePositiveRules(List<QualitativeKeywordRule> keywordRules) {
        if (keywordRules == null || keywordRules.isEmpty()) {
            return DEFAULT_POSITIVE_RULES;
        }
        return keywordRules;
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