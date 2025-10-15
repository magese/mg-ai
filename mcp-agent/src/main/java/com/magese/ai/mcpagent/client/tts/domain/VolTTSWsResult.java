package com.magese.ai.mcpagent.client.tts.domain;

/**
 * 火山云语音处理结果
 *
 * @author Magese
 * @since 2025/10/13 10:01
 */
public record VolTTSWsResult(
        byte[] audioBytes,
        String voice,
        String encoding,
        String fileName,
        long fileSize
) {
}
