package com.magese.ai.mcpserver.config;

import com.magese.ai.mcpserver.service.CalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具提供者配置
 *
 * @author Magese
 * @since 2025/8/27 09:40
 */
@Slf4j
@Configuration
public class ToolProviderConfig {

    @Bean
    public ToolCallbackProvider calculatorTools(CalculatorService calculatorService) {
        log.info("register calculatorTools: {}", calculatorService);
        return MethodToolCallbackProvider.builder().toolObjects(calculatorService).build();
    }
}
