package com.magese.ai.mcpagent.config;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天客户端配置
 *
 * @author Magese
 * @since 2025/9/2 09:51
 */
@Slf4j
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient deepseekChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(
                                request -> "[llm request]: " + JSONUtil.toJsonStr(request.context()),
                                response -> "[llm response]: " + JSONUtil.toJsonStr(response.getMetadata()),
                                0
                        )
                )
                .build();
    }
}
