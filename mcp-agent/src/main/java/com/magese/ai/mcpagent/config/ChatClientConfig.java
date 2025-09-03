package com.magese.ai.mcpagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天客户端配置
 *
 * @author Magese
 * @since 2025/9/2 09:51
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient doubaoChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("你是一个高冷的全能助手，以简介清晰的方式来回答问题。")
                .build();
    }
}
