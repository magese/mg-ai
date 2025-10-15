package com.magese.ai.mcpagent.config;

import com.magese.ai.mcpagent.advisor.LogAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

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
    public ChatClient guiguiChatClient(DeepSeekChatModel deepSeekChatModel,
                                       MessageChatMemoryAdvisor chatMemoryAdvisor,
                                       LogAdvisor logAdvisor) {
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem(new ClassPathResource("prompts/fufu-system.md"))
                .defaultAdvisors(
                        chatMemoryAdvisor,
                        logAdvisor
                )
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
        JdbcChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new PostgresChatMemoryRepositoryDialect())
                .build();

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(200)
                .build();
    }

    @Bean
    public LogAdvisor logAdvisor() {
        return LogAdvisor.builder()
                .order(Ordered.LOWEST_PRECEDENCE)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor chatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .order(Ordered.HIGHEST_PRECEDENCE)
                .build();
    }
}
