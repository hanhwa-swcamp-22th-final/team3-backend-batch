package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeCommentAnalysis;
import com.ohgiraffers.team3backendbatch.domain.scoring.Chunk;
import com.ohgiraffers.team3backendbatch.domain.scoring.ChunkContribution;
import com.ohgiraffers.team3backendbatch.domain.scoring.KeywordScoreResult;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeChunkSplitter;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeKeywordScorer;
import com.ohgiraffers.team3backendbatch.domain.scoring.QualitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.NlpAnalysisGateway;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.dto.NlpAnalysisResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Analyzes comment text only and returns the raw NLP-based qualitative score.
 */
@Component
@RequiredArgsConstructor
public class QualitativeCommentAnalyzer {

    private final NlpAnalysisGateway nlpAnalysisGateway;
    private final QualitativeChunkSplitter qualitativeChunkSplitter;
    private final QualitativeKeywordScorer qualitativeKeywordScorer;
    private final QualitativeScoreCalculator qualitativeScoreCalculator;

    public QualitativeCommentAnalysis analyze(String commentText) {
        List<Chunk> chunks = qualitativeChunkSplitter.splitIntoChunks(commentText);
        List<ChunkContribution> contributions = new ArrayList<>();
        Set<String> matchedKeywordSet = new LinkedHashSet<>();
        boolean negationDetected = false;

        for (Chunk chunk : chunks) {
            NlpAnalysisResponse response = nlpAnalysisGateway.annotateText(chunk.getText());
            KeywordScoreResult keywordScore = qualitativeKeywordScorer.scoreKeywords(
                chunk.getText(),
                response.getKeywordLemmas()
            );
            BigDecimal chunkScore = qualitativeScoreCalculator.calculateChunkScore(
                response.getSentimentScore(),
                keywordScore.getKeywordWeightSum(),
                response.isNegationDetected()
            );
            contributions.add(new ChunkContribution(chunkScore, chunk.isContrastive()));
            matchedKeywordSet.addAll(keywordScore.getMatchedKeywords());
            negationDetected = negationDetected || response.isNegationDetected();
        }

        BigDecimal contextWeight = qualitativeKeywordScorer.determineContextWeight(
            commentText,
            matchedKeywordSet.size()
        );
        BigDecimal commentRawScore = qualitativeScoreCalculator.calculateWeightedAverage(contributions);
        BigDecimal officialRawScore = qualitativeScoreCalculator.applyContextWeight(commentRawScore, contextWeight);
        BigDecimal commentSQual = qualitativeScoreCalculator.normalizeToSQual(officialRawScore);

        return QualitativeCommentAnalysis.builder()
            .commentRawScore(commentRawScore)
            .officialRawScore(officialRawScore)
            .commentSQual(commentSQual)
            .matchedKeywordCount(matchedKeywordSet.size())
            .matchedKeywords(List.copyOf(matchedKeywordSet))
            .contextWeight(contextWeight)
            .negationDetected(negationDetected)
            .build();
    }
}