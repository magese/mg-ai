package com.magese.ai.mcpserver;

import com.magese.ai.mcpserver.service.CalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * MCP服务端
 *
 * @author Magese
 * @since 2025/8/26 17:20
 */
@Slf4j
@SpringBootApplication
public class McpServerApp {

    public static void main(String[] args) {
        try {
            log.info("Starting MCP Server");
            SpringApplication.run(McpServerApp.class, args);
            log.info("MCP Server started");
        }  catch (Exception e) {
            log.error("Error starting MCP Server", e);
        }
    }

    @Bean
    public ToolCallbackProvider calculatorTools(CalculatorService calculatorService) {
        log.info("register calculatorTools: {}", calculatorService);
        return MethodToolCallbackProvider.builder().toolObjects(calculatorService).build();
    }
}
