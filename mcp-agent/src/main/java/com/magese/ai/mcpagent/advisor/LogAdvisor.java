package com.magese.ai.mcpagent.advisor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * 日志顾问
 *
 * @author Magese
 * @since 2025/10/10 11:46
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LogAdvisor implements CallAdvisor, StreamAdvisor {

    @Builder.Default
    private String name = "LogAdvisor";
    @Builder.Default
    private int order = Ordered.LOWEST_PRECEDENCE;

    @NonNull
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, @NonNull CallAdvisorChain chain) {
        long startTime = System.currentTimeMillis();
        log.info("🚀 开始处理AI请求: {}", request.prompt().getUserMessage().getText());

        try {
            ChatClientResponse response = chain.nextCall(request);
            long duration = System.currentTimeMillis() - startTime;

            log.info("✅ AI请求处理完成 - 耗时: {}ms, Token使用: {}", duration, extractTokenUsage(response));
            return response;

        } catch (Exception e) {
            log.error("❌ AI请求处理失败", e);
            throw e;
        }
    }

    @NonNull
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        log.info("🌊 开始流式AI请求: {}", request.prompt().getUserMessage().getText());

        return chain.nextStream(request)
                .doOnNext(response -> log.debug("收到流式响应片段"))
                .doOnComplete(() -> log.info("流式请求完成"))
                .doOnError(error -> log.error("流式请求失败", error));
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    private String extractTokenUsage(ChatClientResponse response) {
        return Objects.requireNonNull(response.chatResponse()).getMetadata().getUsage().toString();
    }
}
