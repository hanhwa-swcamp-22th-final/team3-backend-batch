package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeKeywordRuleLoader;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.dto.DomainKeywordRuleRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeKeywordRuleMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class DbDomainKeywordRuleLoader implements QualitativeKeywordRuleLoader {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final QualitativeKeywordRuleMapper qualitativeKeywordRuleMapper;

    @Override
    public List<QualitativeKeywordRule> loadActiveKeywordRules() {
        return qualitativeKeywordRuleMapper.findActiveDomainKeywordRules().stream()
            .map(this::toRule)
            .filter(rule -> rule.getScoreWeight().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    private QualitativeKeywordRule toRule(DomainKeywordRuleRow row) {
        BigDecimal baseScore = row.getBaseScore() == null ? BigDecimal.ZERO : row.getBaseScore();
        BigDecimal weight = row.getWeight() == null ? BigDecimal.ZERO : row.getWeight();
        BigDecimal scoreWeight = baseScore.multiply(weight).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return new QualitativeKeywordRule(row.getKeyword(), scoreWeight);
    }
}