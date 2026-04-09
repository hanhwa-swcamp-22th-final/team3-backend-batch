package com.ohgiraffers.team3backendbatch.domain.qualitative.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.KeywordScoreResult;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QualitativeKeywordScorerTest {

    private final QualitativeKeywordScorer scorer = new QualitativeKeywordScorer(() -> List.of(
        new QualitativeKeywordRule("equipment maintenance", BigDecimal.valueOf(0.30)),
        new QualitativeKeywordRule("proposal", BigDecimal.valueOf(0.20)),
        new QualitativeKeywordRule("yield", BigDecimal.valueOf(0.20)),
        new QualitativeKeywordRule("defect reduction", BigDecimal.valueOf(0.25)),
        new QualitativeKeywordRule("analysis", BigDecimal.valueOf(0.15))
    ));

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