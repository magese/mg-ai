package com.magese.ai.mcpagent;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;

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
            ServerProperties serverProperties = SpringUtil.getBean(ServerProperties.class);
            log.info("MCP Agent 启动成功 [http://127.0.0.1:{}]", serverProperties.getPort());
        } catch (Exception e) {
            log.error("MCP Agent 启动异常", e);
        }
    }
}
