package com.magese.ai.mcpagent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
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

    private final ChatClient doubaoChatClient;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {
        log.info("chat user message: {}", message);
        StringBuilder reasoner = new StringBuilder();
        StringBuilder answer = new StringBuilder();

        return doubaoChatClient.prompt()
                .options(DeepSeekChatOptions.builder()
                        .temperature(0.7)
                        .build()
                )
                .user(message)
                .stream()
                .chatResponse()
                .doOnNext(next -> {
                    DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) next.getResult().getOutput();
                    String reasoningContent = deepSeekAssistantMessage.getReasoningContent();
                    String textContent = deepSeekAssistantMessage.getText();
                    answer.append(textContent);
                    reasoner.append(reasoningContent);
                })
                .doOnComplete(() -> {
                    System.out.println(reasoner);
                    System.out.println(answer);
                })
                .mapNotNull(r -> r.getResult().getOutput().toString());
    }
}
