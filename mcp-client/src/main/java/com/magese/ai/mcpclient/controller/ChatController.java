package com.magese.ai.mcpclient.controller;

import com.magese.ai.mcpclient.service.DoubaoAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final DoubaoAssistant doubaoAssistant;

    @GetMapping("/chat")
    public Flux<String> chat(String message) {
        log.info("chat message: {}", message);
        return doubaoAssistant.chat(message);
    }
}
