package com.magese.ai.mcpagent.domain;

/**
 * 音频数据
 *
 * @author Magese
 * @since 2025/10/14 15:00
 */
public record AudioChunk(
        byte[] data,
        String format,
        boolean isFinal
) implements ToJsonString {
}
