package com.ohgiraffers.team3backendbatch.domain.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QualitativeKeywordScorerTest {

    private final QualitativeKeywordScorer scorer = new QualitativeKeywordScorer();

    @Test
    void scoreKeywords_caps_keyword_sum_when_many_positive_keywords_are_matched() {
        KeywordScoreResult result = scorer.scoreKeywords(
            "설비 정비를 제안하고 수율 15%를 개선했다"
        );

        assertThat(result.getMatchedKeywords()).contains("설비 정비", "제안", "수율");
        assertThat(result.getKeywordWeightSum()).isEqualByComparingTo(BigDecimal.valueOf(0.60));
    }

    @Test
    void scoreKeywords_matches_english_keywords_using_text_and_lemmas() {
        KeywordScoreResult result = scorer.scoreKeywords(
            "Equipment maintenance was strong but defect reduction remained insufficient",
            List.of("equipment maintenance", "defect reduction", "insufficient")
        );

        assertThat(result.getMatchedKeywords()).contains("equipment maintenance", "defect reduction", "insufficient");
        assertThat(result.getKeywordWeightSum()).isEqualByComparingTo(BigDecimal.valueOf(0.45));
    }

    @Test
    void determineContextWeight_uses_conservative_scale() {
        assertThat(scorer.determineContextWeight("yield improved", 1)).isEqualByComparingTo("1.0");
        assertThat(scorer.determineContextWeight("yield and lead time improved", 2)).isEqualByComparingTo("1.05");
        assertThat(scorer.determineContextWeight("yield lead time proposal improved 15%", 3)).isEqualByComparingTo("1.10");
    }
}