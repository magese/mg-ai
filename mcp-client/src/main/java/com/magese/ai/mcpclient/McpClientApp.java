package com.magese.ai.mcpclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
