package com.magese.ai.mcpagent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP代理
 *
 * @author Magese
 * @since 2025/9/1 15:33
 */
@Slf4j
@SpringBootApplication
public class McpAgentApp {

    public static void main(String[] args) {
        try {
            log.info("MCP Agent 开始启动...");
            SpringApplication.run(McpAgentApp.class, args);
            log.info("MCP Agent 启动成功");
        } catch (Exception e) {
            log.error("MCP Agent 启动异常", e);
        }
    }
}
