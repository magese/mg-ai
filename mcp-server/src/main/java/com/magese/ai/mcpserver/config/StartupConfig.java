package com.magese.ai.mcpserver.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 启动配置
 *
 * @author Magese
 * @since 2025/8/27 09:42
 */
@Configuration
public class StartupConfig {

    @Bean
    public CommandLineRunner startupInfo() {
        return args -> {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("MCP Server is starting...");
            System.out.println("SSE endpoint: http://127.0.0.1:13700/sse");
            System.out.println("Health check: http://127.0.0.1:13700/actuator/health");
            System.out.println("=".repeat(60) + "\n");
        };
    }
}
