package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeSentenceAnalysisEvent {

    private BigDecimal nlpSentiment;
    private Integer matchedKeywordCount;
    private List<String> matchedKeywords;
    private List<MatchedKeywordDetailEvent> matchedKeywordDetails;
    private BigDecimal contextWeight;
    private Boolean negationDetected;
}
