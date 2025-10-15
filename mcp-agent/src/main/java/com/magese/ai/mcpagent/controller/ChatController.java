package com.magese.ai.mcpagent.controller;

import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsResult;
import com.magese.ai.mcpagent.domain.ChatEvent;
import com.magese.ai.mcpagent.service.TTSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 聊天API
 *
 * @author Magese
 * @since 2025/8/29 16:14
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatClient guiguiChatClient;
    private final TTSService ttsService;

    @GetMapping(value = "/v2/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEvent> chat(@RequestParam String message) {
        log.info("Chat User Message:\n[{}]", message);

        // 检查是否有活跃会话
        if (ttsService.hasActiveSession()) {
            return Flux.error(new IllegalStateException(
                    "TTS service is busy with session: " + ttsService.getCurrentSessionId()));
        }

        return guiguiChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, 1))
                .stream()
                .content()
                .concatMap(text -> {
                    // 对于每个 LLM 文本片段：
                    // 1. 立即返回文本事件给前端
                    // 2. 发送到 TTS 服务进行语音合成
                    Flux<ChatEvent> textEvent = Flux.just(ChatEvent.textEvent(text));
                    Flux<ChatEvent> audioEvents = ttsService.synthesizeSpeechStream(text)
                            .map(ChatEvent::audioEvent)
                            .onErrorResume(e -> {
                                log.error("Error synthesizing text: {}", text, e);
                                return Flux.empty(); // 继续处理后续文本
                            });
                    return Flux.concat(textEvent, audioEvents);
                })
                .concatWith(Flux.defer(() -> {
                    // LLM 响应结束后，结束 TTS 会话并处理剩余音频
                    return ttsService.endSession()
                            .map(ChatEvent::audioEvent)
                            .onErrorResume(e -> {
                                log.error("Error ending TTS session", e);
                                return Flux.empty();
                            })
                            .concatWith(Flux.just(ChatEvent.endEvent()));
                }))
                .doOnCancel(() -> {
                    // 如果流被取消，强制结束 TTS 会话
                    log.warn("Chat stream cancelled, forcing TTS session end");
                    ttsService.forceEndSession();
                })
                .doOnError(error -> {
                    // 发生错误时强制结束 TTS 会话
                    log.error("Chat stream error, forcing TTS session end", error);
                    ttsService.forceEndSession();
                })
                .onErrorResume(error -> {
                    // 返回错误信息给前端
                    return Flux.just(
                            ChatEvent.textEvent("抱歉，发生了错误: " + error.getMessage()),
                            ChatEvent.endEvent()
                    );
                });
    }

    @GetMapping(value = "/v1/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatV1(@RequestParam String message) {
        log.info("ChatV1 User Message:\n[{}]", message);
        return guiguiChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, 1))
                .stream()
                .chatClientResponse()
                .mapNotNull(r -> Objects.requireNonNull(r.chatResponse()).getResult().getOutput().getText());
    }

    @GetMapping(value = "/tts", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<byte[]>> tts(@RequestParam String text) {
        VolTTSWsResult result = ttsService.synthesizeSpeech(text);
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", result.fileName()))
                .header(HttpHeaders.CONTENT_TYPE, String.format("audio/%s", result.encoding()))
                .body(result.audioBytes())
        );
    }
}
