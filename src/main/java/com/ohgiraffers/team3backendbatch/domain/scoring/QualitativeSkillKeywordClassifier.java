package com.ohgiraffers.team3backendbatch.domain.scoring;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.MatchedKeywordDetail;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QualitativeSkillKeywordClassifier {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal ONE = new BigDecimal("1.00");
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    public Map<String, BigDecimal> classifyMatchedKeywords(List<MatchedKeywordDetail> matchedKeywordDetails) {
        Map<String, BigDecimal> categoryWeights = new LinkedHashMap<>();
        if (matchedKeywordDetails == null) {
            return categoryWeights;
        }

        for (MatchedKeywordDetail detail : matchedKeywordDetails) {
            String category = resolveSkillCategory(detail);
            if (category != null) {
                categoryWeights.merge(category, resolveSignalWeight(detail), BigDecimal::add);
            }
        }
        return categoryWeights;
    }

    public Map<String, BigDecimal> toSkillScores(BigDecimal qualitativeScore, List<MatchedKeywordDetail> matchedKeywordDetails) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        if (qualitativeScore == null || matchedKeywordDetails == null || matchedKeywordDetails.isEmpty()) {
            return result;
        }

        Map<String, BigDecimal> categoryWeights = classifyMatchedKeywords(matchedKeywordDetails);
        BigDecimal totalWeight = categoryWeights.values().stream().reduce(ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return result;
        }

        BigDecimal normalizedQualitativeScore = sanitize(qualitativeScore);
        for (Map.Entry<String, BigDecimal> entry : categoryWeights.entrySet()) {
            BigDecimal share = entry.getValue().divide(totalWeight, 4, RoundingMode.HALF_UP);
            result.put(entry.getKey(), sanitize(normalizedQualitativeScore.multiply(share)));
        }
        return result;
    }

    private String resolveSkillCategory(MatchedKeywordDetail detail) {
        if (detail == null) {
            return null;
        }

        String domainCategory = detail.getDomainCompetencyCategory();
        if (domainCategory != null && !domainCategory.isBlank()) {
            switch (domainCategory) {
                case "SAFETY":
                    return "SAFETY_COMPLIANCE";
                case "INNOVATION":
                    return "INNOVATION_PROPOSAL";
                case "COLLABORATION":
                    return "TECHNICAL_TRANSFER";
                case "LEADERSHIP":
                    return null;
                case "TECHNICAL_COMPETENCE":
                case "OTHERS":
                default:
                    break;
            }
        }

        return resolveByKeyword(detail.getKeyword());
    }

    private BigDecimal resolveSignalWeight(MatchedKeywordDetail detail) {
        if (detail == null || detail.getScoreWeight() == null) {
            return ONE;
        }
        return detail.getScoreWeight().max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
    }

    private String resolveByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);

        if (containsAny(normalized,
            "equipment", "maintenance", "repair", "recovery", "trouble", "downtime", "setup")) {
            return "EQUIPMENT_RESPONSE";
        }
        if (containsAny(normalized,
            "mentor", "coaching", "training", "handover", "guide", "document", "knowledge")) {
            return "TECHNICAL_TRANSFER";
        }
        if (containsAny(normalized,
            "innovation", "improvement", "proposal", "kaizen", "automation")) {
            return "INNOVATION_PROPOSAL";
        }
        if (containsAny(normalized,
            "compliance", "safety", "hazard", "risk", "ppe")) {
            return "SAFETY_COMPLIANCE";
        }
        if (containsAny(normalized,
            "quality", "defect", "inspection", "yield", "accuracy", "standard")) {
            return "QUALITY_MANAGEMENT";
        }
        if (containsAny(normalized,
            "productivity", "lead time", "cycle", "throughput", "efficiency", "output")) {
            return "PRODUCTIVITY";
        }
        return null;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal sanitize(BigDecimal score) {
        if (score == null) {
            return ZERO;
        }
        return score.max(BigDecimal.ZERO).min(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
}
