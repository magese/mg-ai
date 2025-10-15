package com.magese.ai.mcpagent.domain;

import lombok.Builder;

/**
 * 聊天事件
 *
 * @author Magese
 * @since 2025/10/14 14:54
 */
@Builder
public record ChatEvent(
        EventType type,
        String text,
        byte[] audioData,
        String audioFormat
) implements ToJsonString {

    public enum EventType {
        TEXT, AUDIO, END, PLAY
    }

    public static ChatEvent textEvent(String text) {
        return new ChatEvent(EventType.TEXT, text, null, null);
    }

    public static ChatEvent audioEvent(AudioChunk audio) {
        return new ChatEvent(EventType.AUDIO, null, audio.data(), audio.format());
    }

    public static ChatEvent playEvent(AudioChunk audio) {
        return new ChatEvent(EventType.PLAY, audio.text(), null, audio.format());
    }

    public static ChatEvent endEvent() {
        return ChatEvent.builder()
                .type(EventType.END)
                .build();
    }
}
