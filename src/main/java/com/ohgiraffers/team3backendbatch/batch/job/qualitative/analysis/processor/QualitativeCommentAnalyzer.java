package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeCommentAnalysis;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeSentenceAnalysis;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.Chunk;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.ChunkContribution;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.KeywordScoreResult;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeChunkSplitter;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeKeywordScorer;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
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
        List<NlpAnalysisResponse> responses = new ArrayList<>();
        List<KeywordScoreResult> keywordScores = new ArrayList<>();
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
            responses.add(response);
            keywordScores.add(keywordScore);
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

        List<QualitativeSentenceAnalysis> sentenceAnalyses = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            sentenceAnalyses.add(
                QualitativeSentenceAnalysis.builder()
                    .sentenceOrder(i + 1)
                    .contrastive(chunks.get(i).isContrastive())
                    .nlpSentiment(responses.get(i).getSentimentScore())
                    .matchedKeywordCount(keywordScores.get(i).getMatchedKeywordCount())
                    .matchedKeywords(List.copyOf(keywordScores.get(i).getMatchedKeywords()))
                    .contextWeight(contextWeight)
                    .negationDetected(responses.get(i).isNegationDetected())
                    .build()
            );
        }

        return QualitativeCommentAnalysis.builder()
            .commentRawScore(commentRawScore)
            .officialRawScore(officialRawScore)
            .commentSQual(commentSQual)
            .matchedKeywordCount(matchedKeywordSet.size())
            .matchedKeywords(List.copyOf(matchedKeywordSet))
            .contextWeight(contextWeight)
            .negationDetected(negationDetected)
            .sentenceAnalyses(List.copyOf(sentenceAnalyses))
            .build();
    }
}