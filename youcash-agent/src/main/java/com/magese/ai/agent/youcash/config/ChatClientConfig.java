package com.magese.ai.agent.youcash.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
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
    public ChatClient guiguiChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.builder(deepSeekChatModel).build();
    }

}
