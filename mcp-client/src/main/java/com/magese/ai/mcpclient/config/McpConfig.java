package com.magese.ai.mcpclient.config;

import com.magese.ai.mcpclient.service.DoubaoAssistant;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * MCP配置
 *
 * @author Magese
 * @since 2025/8/29 17:52
 */
@Configuration
public class McpConfig {

    @Value("${mg.mcp.sse.url:http://127.0.0.1:13700/sse}")
    private String mcpSseUrl;

    @Bean
    public McpTransport getMcpTransport() {
        return new HttpMcpTransport.Builder()
                .sseUrl(mcpSseUrl)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public McpClient getMcpClient(McpTransport transport) {
        return new DefaultMcpClient.Builder()
                .transport(transport)
                .build();
    }

    @Bean
    public ToolProvider getToolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();
    }

    @Bean
    public DoubaoAssistant getDoubaoAssistant(StreamingChatModel doubaoAssistant, ToolProvider toolProvider) {
        return AiServices.builder(DoubaoAssistant.class)
                .streamingChatModel(doubaoAssistant)
                .toolProvider(toolProvider)
                .build();
    }

}
