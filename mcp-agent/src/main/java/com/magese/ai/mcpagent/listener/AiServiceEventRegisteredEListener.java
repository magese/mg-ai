package com.magese.ai.mcpagent.listener;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI服务事件监听
 *
 * @author Magese
 * @since 2025/9/1 16:28
 */
@Slf4j
@Component
public class AiServiceEventRegisteredEListener implements ApplicationListener<AiServiceRegisteredEvent> {

    @Override
    public void onApplicationEvent(AiServiceRegisteredEvent event) {
        Class<?> aiServiceClass = event.aiServiceClass();
        List<ToolSpecification> toolSpecifications = event.toolSpecifications();
        for (int i = 0; i < toolSpecifications.size(); i++) {
            log.info("{}: [Tool-{}]: {}", aiServiceClass.getSimpleName(), i + 1, toolSpecifications.get(i));
        }
    }
}
