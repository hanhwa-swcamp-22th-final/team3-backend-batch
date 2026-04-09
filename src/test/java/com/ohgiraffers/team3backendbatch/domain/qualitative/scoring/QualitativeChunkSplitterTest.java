package com.ohgiraffers.team3backendbatch.domain.qualitative.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.Chunk;

import java.util.List;
import org.junit.jupiter.api.Test;

class QualitativeChunkSplitterTest {

    private static final String KOREAN_COMMENT = "설비 정비는 잘했지만 불량 저감은 미흡했다";
    private static final String KOREAN_FIRST_CHUNK = "설비 정비는 잘했";
    private static final String KOREAN_SECOND_CHUNK = "지만 불량 저감은 미흡했다";

    private static final String ENGLISH_COMMENT = "Equipment maintenance was strong but defect reduction was insufficient.";
    private static final String ENGLISH_FIRST_CHUNK = "Equipment maintenance was strong";
    private static final String ENGLISH_SECOND_CHUNK = "but defect reduction was insufficient.";

    private final QualitativeChunkSplitter splitter = new QualitativeChunkSplitter();

    @Test
    void splitIntoChunks_marks_later_chunk_as_contrastive_when_korean_marker_exists() {
        List<Chunk> chunks = splitter.splitIntoChunks(KOREAN_COMMENT);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).isContrastive()).isFalse();
        assertThat(chunks.get(0).getText()).isEqualTo(KOREAN_FIRST_CHUNK);
        assertThat(chunks.get(1).isContrastive()).isTrue();
        assertThat(chunks.get(1).getText()).isEqualTo(KOREAN_SECOND_CHUNK);
    }

    @Test
    void splitIntoChunks_marks_later_chunk_as_contrastive_when_english_marker_exists() {
        List<Chunk> chunks = splitter.splitIntoChunks(ENGLISH_COMMENT);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).isContrastive()).isFalse();
        assertThat(chunks.get(0).getText()).isEqualTo(ENGLISH_FIRST_CHUNK);
        assertThat(chunks.get(1).isContrastive()).isTrue();
        assertThat(chunks.get(1).getText()).isEqualTo(ENGLISH_SECOND_CHUNK);
    }
}