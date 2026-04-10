package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeCommentAnalysis;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationScoreResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeEvaluationProcessor
    implements ItemProcessor<QualitativeEvaluationAggregate, QualitativeAnalysisResult> {

    private final QualitativeCommentAnalyzer qualitativeCommentAnalyzer;
    private final QualitativeEvaluationScorePolicy qualitativeEvaluationScorePolicy;

    @Override
    public QualitativeAnalysisResult process(QualitativeEvaluationAggregate item) {
        if (item.getEvaluationLevel() != null && item.getEvaluationLevel() >= 3L) {
            throw new IllegalStateException("Final evaluation level is not processed by qualitative analysis batch.");
        }

        boolean reusedPreviousScore = item.getEvaluationLevel() != null
            && item.getEvaluationLevel() == 2L
            && item.getSecondEvaluationMode() == SecondEvaluationMode.KEEP_FIRST_SCORE;

        QualitativeCommentAnalysis commentAnalysis = reusedPreviousScore
            ? null
            : qualitativeCommentAnalyzer.analyze(item.getCommentText(), item.getKeywordRules());
        QualitativeEvaluationScoreResult scoreResult = qualitativeEvaluationScorePolicy.apply(item, commentAnalysis);

        return QualitativeAnalysisResult.builder()
            .evaluationId(item.getEvaluationId())
            .evaluatorId(item.getEvaluatorId())
            .evaluationLevel(item.getEvaluationLevel())
            .secondEvaluationMode(item.getSecondEvaluationMode())
            .reusedPreviousScore(reusedPreviousScore)
            .baseRawScore(item.getBaseRawScore())
            .commentRawScore(commentAnalysis != null ? commentAnalysis.getCommentRawScore() : null)
            .commentSQual(commentAnalysis != null ? commentAnalysis.getCommentSQual() : null)
            .adjustmentScore(scoreResult.getAdjustmentScore())
            .squalRaw(scoreResult.getFinalRawScore())
            .originalSQual(scoreResult.getOriginalSQual())
            .sQual(scoreResult.getFinalSQual())
            .normalizedTier(scoreResult.getNormalizedTier())
            .matchedKeywordCount(commentAnalysis != null ? commentAnalysis.getMatchedKeywordCount() : 0)
            .matchedKeywords(commentAnalysis != null ? commentAnalysis.getMatchedKeywords() : List.of())
            .contextWeight(commentAnalysis != null ? commentAnalysis.getContextWeight() : null)
            .negationDetected(commentAnalysis != null && commentAnalysis.isNegationDetected())
            .algorithmVersion(item.getAnalysisVersion())
            .algorithmVersionId(item.getAlgorithmVersionId())
            .analysisStatus("COMPLETED")
            .analyzedAt(LocalDateTime.now())
            .biasCorrected(false)
            .sentenceAnalyses(commentAnalysis != null ? commentAnalysis.getSentenceAnalyses() : List.of())
            .build();
    }
}