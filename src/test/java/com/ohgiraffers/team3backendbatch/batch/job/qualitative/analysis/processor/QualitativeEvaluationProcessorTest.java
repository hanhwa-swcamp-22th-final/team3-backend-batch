package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeChunkSplitter;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeKeywordScorer;
import com.ohgiraffers.team3backendbatch.domain.qualitative.scoring.QualitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.NlpAnalysisGateway;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.dto.NlpAnalysisResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QualitativeEvaluationProcessorTest {

    private static final String COMMENT = "equipment maintenance proposal improved yield 15%";

    @Mock
    private NlpAnalysisGateway nlpAnalysisGateway;

    @Test
    void process_shouldBuildBaseRawScoreForFirstEvaluation() {
        QualitativeEvaluationProcessor processor = createProcessor();

        when(nlpAnalysisGateway.annotateText(anyString()))
            .thenReturn(mockPositiveResponse());

        QualitativeEvaluationAggregate aggregate = new QualitativeEvaluationAggregate(
            1L,
            202604L,
            1001L,
            9001L,
            9101L,
            1L,
            null,
            null,
            COMMENT,
            "TEXT",
            "v1",
            LocalDateTime.of(2026, 4, 6, 9, 0),
            List.of()
        );

        QualitativeAnalysisResult result = processor.process(aggregate);

        assertThat(result.getEvaluationId()).isEqualTo(1L);
        assertThat(result.getEvaluationLevel()).isEqualTo(1L);
        assertThat(result.getSecondEvaluationMode()).isNull();
        assertThat(result.isReusedPreviousScore()).isFalse();
        assertThat(result.getBaseRawScore()).isNull();
        assertThat(result.getAlgorithmVersion()).isEqualTo("v1");
        assertThat(result.getAnalysisStatus()).isEqualTo("COMPLETED");
        assertThat(result.getMatchedKeywordCount()).isGreaterThanOrEqualTo(1);
        assertThat(result.getMatchedKeywords()).isNotEmpty();
        assertThat(result.getCommentRawScore()).isEqualByComparingTo("1.2000");
        assertThat(result.getSqualRaw()).isEqualByComparingTo("87.50");
        assertThat(result.getCommentSQual()).isEqualByComparingTo("87.50");
        assertThat(result.getAdjustmentScore()).isEqualByComparingTo("0.0000");
        assertThat(result.getOriginalSQual()).isEqualByComparingTo("87.50");
        assertThat(result.isBiasCorrected()).isFalse();
        assertThat(result.getSQual()).isEqualByComparingTo("87.50");
        assertThat(result.getNormalizedTier()).isEqualTo("S");
    }

    @Test
    void process_shouldBuildAdjustedRawScoreForSecondEvaluation() {
        QualitativeEvaluationProcessor processor = createProcessor();

        when(nlpAnalysisGateway.annotateText(anyString()))
            .thenReturn(mockPositiveResponse());

        QualitativeEvaluationAggregate aggregate = new QualitativeEvaluationAggregate(
            2L,
            202604L,
            1001L,
            9002L,
            9102L,
            2L,
            SecondEvaluationMode.ANALYZE_COMMENT,
            BigDecimal.valueOf(87.50),
            COMMENT,
            "TEXT",
            "v1",
            LocalDateTime.of(2026, 4, 6, 10, 0),
            List.of()
        );

        QualitativeAnalysisResult result = processor.process(aggregate);

        assertThat(result.getEvaluationLevel()).isEqualTo(2L);
        assertThat(result.getSecondEvaluationMode()).isEqualTo(SecondEvaluationMode.ANALYZE_COMMENT);
        assertThat(result.isReusedPreviousScore()).isFalse();
        assertThat(result.getBaseRawScore()).isEqualByComparingTo("87.50");
        assertThat(result.getCommentRawScore()).isEqualByComparingTo("1.2000");
        assertThat(result.getCommentSQual()).isEqualByComparingTo("87.50");
        assertThat(result.getAdjustmentScore()).isEqualByComparingTo("14.20");
        assertThat(result.getSqualRaw()).isEqualByComparingTo("100.00");
        assertThat(result.getOriginalSQual()).isEqualByComparingTo("100.00");
        assertThat(result.getSQual()).isEqualByComparingTo("100.00");
        assertThat(result.getNormalizedTier()).isEqualTo("S");
    }

    @Test
    void process_shouldReuseFirstRawScoreForSecondEvaluationWithoutCommentAnalysis() {
        QualitativeEvaluationProcessor processor = createProcessor();

        QualitativeEvaluationAggregate aggregate = new QualitativeEvaluationAggregate(
            4L,
            202604L,
            1001L,
            9004L,
            9104L,
            2L,
            SecondEvaluationMode.KEEP_FIRST_SCORE,
            BigDecimal.valueOf(87.50),
            null,
            "TEXT",
            "v1",
            LocalDateTime.of(2026, 4, 6, 10, 30),
            List.of()
        );

        QualitativeAnalysisResult result = processor.process(aggregate);

        assertThat(result.getSecondEvaluationMode()).isEqualTo(SecondEvaluationMode.KEEP_FIRST_SCORE);
        assertThat(result.isReusedPreviousScore()).isTrue();
        assertThat(result.getCommentRawScore()).isNull();
        assertThat(result.getCommentSQual()).isNull();
        assertThat(result.getMatchedKeywordCount()).isZero();
        assertThat(result.getMatchedKeywords()).isEmpty();
        assertThat(result.getAdjustmentScore()).isEqualByComparingTo("0.0000");
        assertThat(result.getSqualRaw()).isEqualByComparingTo("87.50");
        assertThat(result.getOriginalSQual()).isEqualByComparingTo("87.50");
        assertThat(result.getSQual()).isEqualByComparingTo("87.50");
        assertThat(result.getNormalizedTier()).isEqualTo("S");

        verify(nlpAnalysisGateway, never()).annotateText(anyString());
    }

    @Test
    void process_shouldFailWhenSecondEvaluationDoesNotHaveBaseRawScore() {
        QualitativeEvaluationProcessor processor = createProcessor();

        when(nlpAnalysisGateway.annotateText(anyString()))
            .thenReturn(mockPositiveResponse());

        QualitativeEvaluationAggregate aggregate = new QualitativeEvaluationAggregate(
            3L,
            202604L,
            1001L,
            9003L,
            9103L,
            2L,
            SecondEvaluationMode.ANALYZE_COMMENT,
            null,
            COMMENT,
            "TEXT",
            "v1",
            LocalDateTime.of(2026, 4, 6, 11, 0),
            List.of()
        );

        assertThatThrownBy(() -> processor.process(aggregate))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Base first evaluation raw score is missing");
    }

    @Test
    void process_shouldRejectFinalEvaluationLevel() {
        QualitativeEvaluationProcessor processor = createProcessor();

        QualitativeEvaluationAggregate aggregate = new QualitativeEvaluationAggregate(
            5L,
            202604L,
            1001L,
            9005L,
            9105L,
            3L,
            null,
            BigDecimal.valueOf(1.3200),
            null,
            "TEXT",
            "v1",
            LocalDateTime.of(2026, 4, 6, 11, 30),
            List.of()
        );

        assertThatThrownBy(() -> processor.process(aggregate))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Final evaluation level is not processed");

        verify(nlpAnalysisGateway, never()).annotateText(anyString());
    }

    private QualitativeEvaluationProcessor createProcessor() {
        QualitativeScoreCalculator calculator = new QualitativeScoreCalculator();
        return new QualitativeEvaluationProcessor(
            new QualitativeCommentAnalyzer(
                nlpAnalysisGateway,
                new QualitativeChunkSplitter(),
                new QualitativeKeywordScorer(),
                calculator
            ),
            new QualitativeEvaluationScorePolicy(calculator)
        );
    }

    private NlpAnalysisResponse mockPositiveResponse() {
        return new NlpAnalysisResponse(
            BigDecimal.valueOf(0.6),
            false,
            List.of(
                "equipment",
                "maintenance",
                "proposal",
                "yield",
                "improve"
            )
        );
    }
}
