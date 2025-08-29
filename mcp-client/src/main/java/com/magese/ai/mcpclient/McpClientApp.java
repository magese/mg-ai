package com.magese.ai.mcpclient;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * MCP客户端
 *
 * @author Magese
 * @since 2025/8/29 16:06
 */
@Slf4j
@SpringBootApplication
public class McpClientApp {

    public static void main(String[] args) {
        try {
            log.info("Starting MCP Client");
            SpringApplication.run(McpClientApp.class, args);
            log.info("MCP Client started");
        } catch (Exception e) {
            log.error("Error starting MCP Client", e);
        }
    }

    @Bean
    public McpTransport getMcpTransport() {
        return new HttpMcpTransport.Builder()
                .sseUrl("http://127.0.0.1:13700/sse")
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

    @Bean("calcTool")
    public ToolProvider getToolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();
    }
}
