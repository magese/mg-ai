package com.magese.ai.mcpclient.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * AI助手
 *
 * @author Magese
 * @since 2025/8/29 16:20
 */
@AiService(streamingChatModel = "ep-20250829155452-dp8pl")
public interface DoubaoAssistant {

    @SystemMessage("你是一个高级智能助手，请用最简短且清晰的答案来回答用户的问题。")
    Flux<String> chat(String message);
}
