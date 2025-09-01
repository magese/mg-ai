package com.magese.ai.mcpagent.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 聊天模型监听
 *
 * @author Magese
 * @since 2025/9/1 16:32
 */
@Slf4j
@Component
public class McpAgentChatModelListener implements ChatModelListener {

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        log.info("模型请求：{}", requestContext.chatRequest());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        log.info("模型响应：{}", responseContext.chatResponse());
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        log.info("模型异常：{}", errorContext.error().getMessage());
    }
}
