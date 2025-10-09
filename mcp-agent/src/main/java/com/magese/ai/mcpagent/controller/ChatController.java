package com.magese.ai.mcpagent.controller;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
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

    private final DeepSeekChatModel deepSeekChatModel;

    private static final StringBuilder REASONER = new StringBuilder();
    private static final StringBuilder ANSWER = new StringBuilder();

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message, @RequestParam(required = false) boolean reasoning) {
        log.info("chat user message: {}", message);
        if (!REASONER.isEmpty()) {
            REASONER.setLength(0);
        }
        if (!ANSWER.isEmpty()) {
            ANSWER.setLength(0);
        }

        UserMessage userMessage = new UserMessage(message);
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model(reasoning ? DeepSeekApi.ChatModel.DEEPSEEK_REASONER : DeepSeekApi.ChatModel.DEEPSEEK_CHAT)
                .temperature(1.1)
                .build();
        Prompt prompt = new Prompt(userMessage, options);

        return deepSeekChatModel.stream(prompt)
                .doOnNext(this::appendContent)
                .doOnComplete(this::printResult)
                .mapNotNull(r -> {
                            DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) r.getResult().getOutput();
                            String reasoningContent = deepSeekAssistantMessage.getReasoningContent();
                            String textContent = deepSeekAssistantMessage.getText();
                            if (StrUtil.isNotBlank(textContent)) {
                                return textContent;
                            }
                            if (StrUtil.isNotBlank(reasoningContent)) {
                                return reasoningContent;
                            }
                            return "";
                        }
                );
    }

    private void appendContent(ChatResponse next) {
        DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) next.getResult().getOutput();
        String reasoningContent = deepSeekAssistantMessage.getReasoningContent();
        String textContent = deepSeekAssistantMessage.getText();
        if (StrUtil.isNotBlank(textContent)) {
            ANSWER.append(textContent);
        }
        if (StrUtil.isNotBlank(reasoningContent)) {
            REASONER.append(reasoningContent);
        }
    }

    private void printResult() {
        System.out.println("\033[0;34m" + REASONER + "\033[0m");
        System.out.println(ANSWER);
    }
}
