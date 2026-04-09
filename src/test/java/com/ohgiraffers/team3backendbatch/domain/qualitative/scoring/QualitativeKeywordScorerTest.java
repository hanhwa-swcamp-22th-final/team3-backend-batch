package com.ohgiraffers.team3backendbatch.domain.qualitative.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.KeywordScoreResult;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QualitativeKeywordScorerTest {

    private final QualitativeKeywordScorer scorer = new QualitativeKeywordScorer();

    @Test
    void scoreKeywords_caps_keyword_sum_when_many_positive_keywords_are_matched() {
        KeywordScoreResult result = scorer.scoreKeywords(
            "equipment maintenance proposal improved yield 15%"
        );

        assertThat(result.getMatchedKeywords()).contains("equipment maintenance", "proposal", "yield");
        assertThat(result.getKeywordWeightSum()).isEqualByComparingTo(BigDecimal.valueOf(0.60));
    }

    @Test
    void scoreKeywords_matches_admin_keywords_using_text_and_lemmas() {
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
