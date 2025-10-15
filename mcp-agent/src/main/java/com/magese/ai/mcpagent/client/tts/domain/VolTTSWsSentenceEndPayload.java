package com.magese.ai.mcpagent.client.tts.domain;

import lombok.Builder;

import java.util.List;

/**
 * 句子结束Payload
 *
 * @author Magese
 * @since 2025/10/15 14:21
 */
@Builder
public record VolTTSWsSentenceEndPayload(
        List<String> phonemes,
        String text,
        List<Word> words
) {
    @Builder
    public record Word(
            String word,
            double startTime,
            double endTime,
            double confidence
    ) {}
}
