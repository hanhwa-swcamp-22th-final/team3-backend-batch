package com.ohgiraffers.team3backendbatch.domain.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.MatchedKeywordDetail;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QualitativeSkillKeywordClassifierTest {

    private final QualitativeSkillKeywordClassifier classifier = new QualitativeSkillKeywordClassifier();

    @Test
    @DisplayName("stored rule snapshot categories are converted into weighted skill-category qualitative scores")
    void toSkillScoresCalculatesCategorySharesFromStoredRuleDetails() {
        Map<String, BigDecimal> result = classifier.toSkillScores(
            new BigDecimal("80.00"),
            List.of(
                new MatchedKeywordDetail(1L, "maintenance", "TECHNICAL_COMPETENCE", new BigDecimal("0.30")),
                new MatchedKeywordDetail(2L, "repair", "TECHNICAL_COMPETENCE", new BigDecimal("0.20")),
                new MatchedKeywordDetail(3L, "inspection", "TECHNICAL_COMPETENCE", new BigDecimal("0.20")),
                new MatchedKeywordDetail(4L, "productivity", "OTHER", new BigDecimal("0.10")),
                new MatchedKeywordDetail(5L, "safety", "SAFETY", new BigDecimal("0.20"))
            )
        );

        assertThat(result).containsEntry("EQUIPMENT_RESPONSE", new BigDecimal("40.00"));
        assertThat(result).containsEntry("QUALITY_MANAGEMENT", new BigDecimal("16.00"));
        assertThat(result).containsEntry("PRODUCTIVITY", new BigDecimal("8.00"));
        assertThat(result).containsEntry("SAFETY_COMPLIANCE", new BigDecimal("16.00"));
    }

    @Test
    @DisplayName("collaboration category is mapped directly to technical transfer without keyword heuristics")
    void toSkillScoresUsesFrozenCompetencyCategoryFirst() {
        Map<String, BigDecimal> result = classifier.toSkillScores(
            new BigDecimal("90.00"),
            List.of(new MatchedKeywordDetail(10L, "peer support", "COLLABORATION", new BigDecimal("1.00")))
        );

        assertThat(result)
            .containsEntry("TECHNICAL_TRANSFER", new BigDecimal("90.00"))
            .hasSize(1);
    }
}
