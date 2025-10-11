package com.magese.ai.mcpagent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {
        log.info("Chat User Message:\n[{}]", message);
        return guiguiChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, 1))
                .stream()
                .content();
    }

}
