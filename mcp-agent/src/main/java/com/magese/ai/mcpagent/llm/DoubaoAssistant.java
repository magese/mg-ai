package com.magese.ai.mcpagent.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * AI助手
 *
 * @author Magese
 * @since 2025/9/1 15:37
 */
@AiService
public interface DoubaoAssistant {

    @SystemMessage("你是一个高级智能助手，请用最简短且清晰的答案来回答用户的问题。")
    Flux<String> chat(String message);
}
