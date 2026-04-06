package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Splits qualitative comments into contrastive chunks.
 */
@Component
public class QualitativeChunkSplitter {

    private static final MarkerPattern[] CONTRASTIVE_MARKERS = {
        new MarkerPattern("하지만", Pattern.compile("하지만")),
        new MarkerPattern("지만", Pattern.compile("지만")),
        new MarkerPattern("그러나", Pattern.compile("그러나")),
        new MarkerPattern("반면", Pattern.compile("반면")),
        new MarkerPattern("그렇지만", Pattern.compile("그렇지만")),
        new MarkerPattern("but", Pattern.compile("\\bbut\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
        new MarkerPattern("however", Pattern.compile("\\bhowever\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
        new MarkerPattern("although", Pattern.compile("\\balthough\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
        new MarkerPattern("though", Pattern.compile("\\bthough\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
        new MarkerPattern("while", Pattern.compile("\\bwhile\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
        new MarkerPattern("on the other hand", Pattern.compile("on\\s+the\\s+other\\s+hand", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
    };

    public List<Chunk> splitIntoChunks(String commentText) {
        String normalized = commentText == null ? "" : commentText.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        MarkerMatch markerMatch = findNextMarker(normalized);
        if (markerMatch == null) {
            return List.of(new Chunk(normalized, false));
        }

        String before = normalized.substring(0, markerMatch.getIndex()).trim();
        String after = normalized.substring(markerMatch.getIndex()).trim();

        if (before.isBlank()) {
            return List.of(new Chunk(after, true));
        }

        return List.of(
            new Chunk(before, false),
            new Chunk(after, true)
        );
    }

    public boolean detectContrastiveChunk(String text) {
        if (text == null) {
            return false;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        for (MarkerPattern marker : CONTRASTIVE_MARKERS) {
            if (marker.getPattern().matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private MarkerMatch findNextMarker(String text) {
        MarkerMatch result = null;
        for (MarkerPattern marker : CONTRASTIVE_MARKERS) {
            Matcher matcher = marker.getPattern().matcher(text);
            if (matcher.find()) {
                int index = matcher.start();
                if (result == null || index < result.getIndex()) {
                    result = new MarkerMatch(index, marker.getLabel());
                }
            }
        }
        return result;
    }
}