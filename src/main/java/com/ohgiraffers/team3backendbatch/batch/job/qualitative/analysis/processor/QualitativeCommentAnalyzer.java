package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeCommentAnalysis;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeSentenceAnalysis;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.Chunk;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.ChunkContribution;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.KeywordScoreResult;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
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

@Component
@RequiredArgsConstructor
public class QualitativeCommentAnalyzer {

    private final NlpAnalysisGateway nlpAnalysisGateway;
    private final QualitativeChunkSplitter qualitativeChunkSplitter;
    private final QualitativeKeywordScorer qualitativeKeywordScorer;
    private final QualitativeScoreCalculator qualitativeScoreCalculator;

    /**
     * 코멘트를 분석해 정성 점수 계산용 결과를 생성한다.
     * @param commentText 분석할 정성 평가 코멘트
     * @param keywordRules 코멘트에 적용할 키워드 규칙 목록
     * @return 코멘트 분석 및 점수 계산 결과
     */
    public QualitativeCommentAnalysis analyze(String commentText, List<QualitativeKeywordRule> keywordRules) {
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
                response.getKeywordLemmas(),
                keywordRules
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
        BigDecimal commentSQual = qualitativeScoreCalculator.scaleInternalRawToDisplayScore(officialRawScore);

        List<QualitativeSentenceAnalysis> sentenceAnalyses = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            sentenceAnalyses.add(
                QualitativeSentenceAnalysis.builder()
                    .sentenceOrder(i + 1)
                    .contrastive(chunks.get(i).isContrastive())
                    .nlpSentiment(responses.get(i).getSentimentScore())
                    .matchedKeywordCount(keywordScores.get(i).getMatchedKeywordCount())
                    .matchedKeywords(List.copyOf(keywordScores.get(i).getMatchedKeywords()))
                    .matchedKeywordDetails(List.copyOf(keywordScores.get(i).getMatchedKeywordDetails()))
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
