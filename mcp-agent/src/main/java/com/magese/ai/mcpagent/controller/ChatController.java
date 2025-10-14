package com.magese.ai.mcpagent.controller;

import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsResult;
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

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {
        log.info("Chat User Message:\n[{}]", message);
        return guiguiChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, 1))
                .stream()
                .content();
    }

    @GetMapping(value = "/tts", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<byte[]>> tts(@RequestParam String text, @RequestParam(required = false) String voice) {
        VolTTSWsResult result = ttsService.synthesizeSpeech(text, voice);
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", result.fileName()))
                .header(HttpHeaders.CONTENT_TYPE, String.format("audio/%s", result.encoding()))
                .body(result.audioStream().toByteArray())
        );
    }
}
